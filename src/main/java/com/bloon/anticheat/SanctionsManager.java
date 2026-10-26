package com.bloon.anticheat;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SanctionsManager {

    private static final Map<UUID, SanctionData> bannedPlayers = new HashMap<>();
    private static final Map<String, SanctionData> bannedIPs = new HashMap<>(); // Registro de IPs baneadas
    private static final Map<UUID, SanctionData> mutedPlayers = new HashMap<>();

    public static class SanctionData {
        private final UUID uuid;
        private final String playerName;
        private final String reason;
        private final String duration;
        private final String ip;

        public SanctionData(UUID uuid, String playerName, String reason, String duration, String ip) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.reason = reason;
            this.duration = duration;
            this.ip = ip;
        }

        public UUID getUuid() { return uuid; }
        public String getPlayerName() { return playerName; }
        public String getReason() { return reason; }
        public String getDuration() { return duration; }
        public String getIp() { return ip; }
    }

    public static void banPlayer(UUID uuid, String name, String reason, String durationStr) {
        Date expiration = parseDuration(durationStr);
        String kickReason = "§cHas sido BANEADO por IP del servidor.\n§7Razón: §e" + reason + "\n§7Duración: §c" + durationStr;

        // 1. Baneo por nombre
        Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, expiration, "BloonAC");

        // 2. Obtener IP y banearla si está conectado
        String ipAddressStr = null;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            Player player = offlinePlayer.getPlayer();
            InetSocketAddress address = player.getAddress();
            if (address != null && address.getAddress() != null) {
                ipAddressStr = address.getAddress().getHostAddress();
                Bukkit.getBanList(BanList.Type.IP).addBan(ipAddressStr, reason, expiration, "BloonAC");
            }
            player.kickPlayer(kickReason);
        }

        SanctionData data = new SanctionData(uuid, name, reason, durationStr, ipAddressStr);
        bannedPlayers.put(uuid, data);
        if (ipAddressStr != null && !ipAddressStr.isEmpty()) {
            bannedIPs.put(ipAddressStr, data);
        }
    }

    public static void mutePlayer(UUID uuid, String name, String reason, String durationStr) {
        mutedPlayers.put(uuid, new SanctionData(uuid, name, reason, durationStr, null));
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().sendMessage("§c[BloonAC] Has sido SILENCIADO.\n§7Razón: §e" + reason + "\n§7Duración: §c" + durationStr);
        }
    }

    public static void unbanPlayer(UUID uuid) {
        SanctionData data = bannedPlayers.remove(uuid);
        if (data != null) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(data.getPlayerName());
            if (data.getIp() != null && !data.getIp().isEmpty()) {
                Bukkit.getBanList(BanList.Type.IP).pardon(data.getIp());
                bannedIPs.remove(data.getIp());
            }
        } else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            if (op.getName() != null) {
                Bukkit.getBanList(BanList.Type.NAME).pardon(op.getName());
            }
        }
    }

    public static void unbanIP(String ip) {
        bannedIPs.remove(ip);
        Bukkit.getBanList(BanList.Type.IP).pardon(ip);
    }

    public static void unmutePlayer(UUID uuid) {
        mutedPlayers.remove(uuid);
    }

    public static boolean isMuted(UUID uuid) {
        return mutedPlayers.containsKey(uuid);
    }

    public static Map<UUID, SanctionData> getBannedPlayers() {
        return bannedPlayers;
    }

    public static Map<String, SanctionData> getBannedIPs() {
        return bannedIPs;
    }

    public static Map<UUID, SanctionData> getMutedPlayers() {
        return mutedPlayers;
    }

    private static Date parseDuration(String durationStr) {
        if (durationStr == null || durationStr.contains("Permanente")) {
            return null;
        }

        long millisToAdd = 0;
        try {
            String numberOnly = durationStr.replaceAll("[^0-9]", "");
            if (!numberOnly.isEmpty()) {
                long amount = Long.parseLong(numberOnly);
                if (durationStr.contains("Día") || durationStr.contains("Días")) {
                    millisToAdd = amount * 24 * 60 * 60 * 1000L;
                } else if (durationStr.contains("Hora") || durationStr.contains("Horas")) {
                    millisToAdd = amount * 60 * 60 * 1000L;
                }
            }
        } catch (Exception ignored) {}

        if (millisToAdd == 0) return null;
        return new Date(System.currentTimeMillis() + millisToAdd);
    }
}

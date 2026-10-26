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
    private static final Map<UUID, SanctionData> mutedPlayers = new HashMap<>();

    public static class SanctionData {
        private final UUID uuid;
        private final String playerName;
        private final String reason;
        private final String duration;

        public SanctionData(UUID uuid, String playerName, String reason, String duration) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.reason = reason;
            this.duration = duration;
        }

        public UUID getUuid() { return uuid; }
        public String getPlayerName() { return playerName; }
        public String getReason() { return reason; }
        public String getDuration() { return duration; }
    }

    public static void banPlayer(UUID uuid, String name, String reason, String durationStr) {
        bannedPlayers.put(uuid, new SanctionData(uuid, name, reason, durationStr));

        Date expiration = parseDuration(durationStr);
        String kickReason = "§cHas sido BANEADO por IP del servidor.\n§7Razón: §e" + reason + "\n§7Duración: §c" + durationStr;

        // 1. Baneo por nombre
        Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, expiration, "BloonAC");

        // 2. Baneo por IP si el jugador está conectado o se puede obtener su dirección
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            Player player = offlinePlayer.getPlayer();
            InetSocketAddress address = player.getAddress();
            if (address != null && address.getAddress() != null) {
                String ip = address.getAddress().getHostAddress();
                Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, expiration, "BloonAC");
            }
            player.kickPlayer(kickReason);
        }
    }

    public static void mutePlayer(UUID uuid, String name, String reason, String durationStr) {
        mutedPlayers.put(uuid, new SanctionData(uuid, name, reason, durationStr));
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().sendMessage("§c[BloonAC] Has sido SILENCIADO.\n§7Razón: §e" + reason + "\n§7Duración: §c" + durationStr);
        }
    }

    public static void unbanPlayer(UUID uuid) {
        SanctionData data = bannedPlayers.remove(uuid);
        if (data != null) {
            // Perdonar baneo por nombre
            Bukkit.getBanList(BanList.Type.NAME).pardon(data.getPlayerName());
        }
        
        // Intentar perdonar también por IP si el jugador está conectado
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (op.isOnline() && op.getPlayer() != null && op.getPlayer().getAddress() != null) {
            String ip = op.getPlayer().getAddress().getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        }
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

    public static Map<UUID, SanctionData> getMutedPlayers() {
        return mutedPlayers;
    }

    private static Date parseDuration(String durationStr) {
        if (durationStr == null || durationStr.contains("Permanente")) {
            return null; // Baneo permanente
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

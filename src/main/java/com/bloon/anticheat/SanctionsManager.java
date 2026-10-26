package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

    public static void banPlayer(UUID uuid, String name, String reason, String duration) {
        bannedPlayers.put(uuid, new SanctionData(uuid, name, reason, duration));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().kickPlayer("§cHas sido BANEADO del servidor.\n§7Razón: §e" + reason + "\n§7Duración: §c" + duration);
        }
    }

    public static void mutePlayer(UUID uuid, String name, String reason, String duration) {
        mutedPlayers.put(uuid, new SanctionData(uuid, name, reason, duration));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().sendMessage("§c[BloonAC] Has sido silenciado por: §e" + reason + " §7(Duración: " + duration + ")");
        }
    }

    public static void unbanPlayer(UUID uuid) {
        bannedPlayers.remove(uuid);
    }

    public static void unmutePlayer(UUID uuid) {
        mutedPlayers.remove(uuid);
    }

    public static Map<UUID, SanctionData> getBannedPlayers() {
        return bannedPlayers;
    }

    public static Map<UUID, SanctionData> getMutedPlayers() {
        return mutedPlayers;
    }
}

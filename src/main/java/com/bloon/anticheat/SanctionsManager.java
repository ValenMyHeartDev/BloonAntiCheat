package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SanctionsManager {

    private static final Map<UUID, SanctionData> bannedPlayers = new HashMap<>();

    public static class SanctionData {
        private final String playerName;
        private final String reason;
        private final String duration;

        public SanctionData(String playerName, String reason, String duration) {
            this.playerName = playerName;
            this.reason = reason;
            this.duration = duration;
        }

        public String getPlayerName() { return playerName; }
        public String getReason() { return reason; }
        public String getDuration() { return duration; }
    }

    public static void banPlayer(UUID uuid, String name, String reason, String duration) {
        bannedPlayers.put(uuid, new SanctionData(name, reason, duration));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().kickPlayer("§cHas sido sancionado.\n§7Razón: §e" + reason + "\n§7Duración: §c" + duration);
        }
    }

    public static Map<UUID, SanctionData> getBannedPlayers() {
        return bannedPlayers;
    }
}

package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanWaveManager {

    private static final List<PendingPunishment> pendingQueue = new ArrayList<>();

    public static class PendingPunishment {
        private final UUID uuid;
        private final String name;
        private final String reason;

        public PendingPunishment(UUID uuid, String name, String reason) {
            this.uuid = uuid;
            this.name = name;
            this.reason = reason;
        }

        public void execute() {
            SanctionsManager.banPlayer(uuid, name, reason, "Permanente");
        }
    }

    public static void queuePunishment(UUID uuid, String name, String reason) {
        pendingQueue.add(new PendingPunishment(uuid, name, reason));
    }

    public static void startBanWaveTask(Main plugin) {
        // Ejecutar una oleada masiva cada 30 minutos automáticamente
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingQueue.isEmpty()) return;

                Bukkit.broadcastMessage("§8[§cBloonAC§8] §c§l¡Oleada de Baneos (BanWave) ejecutada! Limpiando tramposos del servidor...");
                for (PendingPunishment punishment : pendingQueue) {
                    punishment.execute();
                }
                pendingQueue.clear();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60L * 30L, 20L * 60L * 30L);
    }
}

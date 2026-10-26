package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClickTPCheck implements Listener {

    private final Main plugin;

    public ClickTPCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Omitir solo si el administrador ejecutó /bloon ignore <jugador>
        if (CheckManager.isIgnored(player)) {
            return;
        }

        if (player.getAllowFlight() || player.isInsideVehicle() || player.isGliding() || player.isSwimming() || player.isRiptiding()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.clicktp.enabled", true)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double distanceSquared = from.distanceSquared(to);

        if (distanceSquared > 15.0) {
            event.setCancelled(true);
            player.teleport(from);

            String message = "[BloonAC] " + player.getName() + " falló ClickTP / Teleportación ilegal (Distancia: " + String.format("%.2f", Math.sqrt(distanceSquared)) + "m)";
            Bukkit.getLogger().info(message);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("bloon.alerts")) {
                    staff.sendMessage("§c" + message);
                }
            }
        }
    }
}

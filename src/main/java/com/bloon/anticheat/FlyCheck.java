package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FlyCheck implements Listener {

    private final Main plugin;

    public FlyCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("bloon.bypass") || player.isAllowFlight() || player.isInsideVehicle() || player.isGliding()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.fly.enabled", true)) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Elevación no permitida
        if (deltaY > 0.42 && !player.isOnGround() && player.getFallDistance() == 0) {
            event.setCancelled(true);

            AlertsUtil.AlertType alertType = (deltaY > 1.2) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player,
                "Fly",
                "Elevacion ilegal (DeltaY: " + String.format("%.2f", deltaY) + ")",
                alertType
            );
        }
    }
}

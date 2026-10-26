package com.bloon.anticheat;

import org.bukkit.Bukkit;
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

        // Corrección: Usar getAllowFlight() en lugar de isAllowFlight()
        if (player.hasPermission("bloon.bypass") || player.getAllowFlight() || player.isInsideVehicle() || player.isGliding()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.fly.enabled", true)) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        if (deltaY > 0.42 && !player.isOnGround() && player.getFallDistance() == 0) {
            event.setCancelled(true);

            String message = "[BloonAC] " + player.getName() + " falló Fly (DeltaY: " + String.format("%.2f", deltaY) + ")";
            Bukkit.getLogger().info(message);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("bloon.alerts")) {
                    staff.sendMessage("§c" + message);
                }
            }
        }
    }
}

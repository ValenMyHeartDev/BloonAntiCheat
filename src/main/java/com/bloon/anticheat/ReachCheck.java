package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ReachCheck implements Listener {

    private final Main plugin;

    public ReachCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.reach.enabled", true)) return;

        Entity target = event.getEntity();
        
        // Distancia 3D entre los ojos del jugador y la entidad objetivo
        double distance = player.getEyeLocation().distance(target.getLocation());
        double maxDistance = config.getDouble("checks.reach.max-distance", 3.8); // Ajuste más estricto

        if (distance > maxDistance) {
            event.setCancelled(true);

            AlertsUtil.AlertType alertType = (distance > 4.5) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player, 
                "Reach / KillAura", 
                "Alcance ilegal (Distancia: " + String.format("%.2f", distance) + "m | Máx: " + maxDistance + "m)", 
                alertType
            );
        }
    }
}

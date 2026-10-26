package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class ReachCheck implements Listener {

    private final Main plugin;

    public ReachCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.reach.enabled", true)) return;

        Entity target = event.getEntity();
        
        // Cálculo de distancia exacta (incluyendo diferencias de altura y hitboxes)
        double distance = player.getLocation().distance(target.getLocation());
        
        // Distancia máxima permitida por config (por defecto 4.2 bloques para compensar latencia/ping)
        double maxDistance = config.getDouble("checks.reach.max-distance", 4.2);

        if (distance > maxDistance) {
            event.setCancelled(true);

            // Si la distancia es ridículamente alta (> 5.5), es un KillAura/Reach evidente -> Nivel V
            AlertsUtil.AlertType alertType = (distance > 5.5) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player, 
                "Reach / KillAura", 
                "Distancia: " + String.format("%.2f", distance) + " bloques (Máx: " + maxDistance + ")", 
                alertType
            );
        }
    }
}

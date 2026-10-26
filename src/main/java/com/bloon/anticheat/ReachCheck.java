package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ReachCheck implements Listener {

    private final Main plugin;

    public ReachCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.reach.enabled", true)) return;

        double maxReach = config.getDouble("checks.reach.max-distance", 4.2);
        double distance = player.getLocation().distance(event.getEntity().getLocation());

        if (distance > maxReach) {
            // 1. ACCIÓN PRINCIPAL: Cancela el golpe para que la trampa no haga efecto ni daño
            event.setCancelled(true);

            // 2. Notificar al staff
            AlertsUtil.sendAlert(player, "Reach / Killaura", "Golpe bloqueado a " + String.format("%.2f", distance) + "m");
        }
    }
}

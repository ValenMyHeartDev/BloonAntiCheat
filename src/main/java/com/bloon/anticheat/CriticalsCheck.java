package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CriticalsCheck implements Listener {

    private final Main plugin;

    public CriticalsCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass") || player.isInsideVehicle() || player.isGliding() || player.isFlying()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.combat.criticals", true)) return;

        boolean isOnGround = player.isOnGround();
        float fallDistance = player.getFallDistance();

        // Los clientes de Criticals (Packet / GroundSpoof) simulan una caída o alteran 
        // la distancia de caída (fallDistance > 0) mientras el jugador está pisando el suelo firme.
        if (isOnGround && fallDistance > 0.0f) {
            event.setCancelled(true);

            String message = "[BloonAC] " + player.getName() + " falló Criticals (GroundSpoof/Packet: fallDist=" + String.format("%.2f", fallDistance) + ")";
            Bukkit.getLogger().info(message);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("bloon.alerts")) {
                    staff.sendMessage("§c" + message);
                }
            }
        }
    }
}

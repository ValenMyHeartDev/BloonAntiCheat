package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        // Si el jugador es admin/bypass o tiene permitido el modo vuelo/elytra/vehículo legítimo
        if (player.hasPermission("bloon.bypass") || player.getAllowFlight() || player.isInsideVehicle() || player.isGliding()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.fly.enabled", true)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        double deltaY = to.getY() - from.getY();
        
        // Distancia horizontal (Ejes X y Z)
        double distanceXZ = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));

        // DETECCIÓN 1: Flotar o moverse en el aire sin estar sobre un bloque (Fly / AirWalk / Glide)
        boolean isAirborne = !player.isOnGround() && player.getFallDistance() == 0;

        // Si intenta elevarse sin tocar el suelo O se desplaza horizontalmente flotando
        if (isAirborne && (deltaY >= 0 || distanceXZ > 0.15)) {
            event.setCancelled(true);
            
            // Rebobinar al jugador a su posición previa en el suelo para frenar el desplazamiento
            player.teleport(from);

            String message = "[BloonAC] " + player.getName() + " bloqueado por Fly / Glide (XZ: " + String.format("%.2f", distanceXZ) + "m | Y: " + String.format("%.2f", deltaY) + ")";
            Bukkit.getLogger().info(message);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("bloon.alerts")) {
                    staff.sendMessage("§c" + message);
                }
            }
        }
    }
}

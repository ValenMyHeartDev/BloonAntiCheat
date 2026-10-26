package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class FlyCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Double> lastY = new HashMap<>();

    public FlyCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("bloon.bypass") || player.getAllowFlight() || player.isInsideVehicle() || player.isGliding() || player.isSwimming() || player.isRiptiding()) {
            lastY.remove(player.getUniqueId());
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.fly.enabled", true)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Si está en el suelo, pisando agua, escaleras, lianas o demi-bloques, reseteamos y permitimos todo
        if (player.isOnGround() || player.getLocation().getBlock().isLiquid() || from.getBlock().getType().name().contains("LADDER") || from.getBlock().getType().name().contains("VINE")) {
            lastY.put(player.getUniqueId(), to.getY());
            return;
        }

        double dy = to.getY() - from.getY();
        double previousDy = lastY.getOrDefault(player.getUniqueId(), 0.0);
        
        // La gravedad de Minecraft reduce el movimiento vertical en cada tick (~ -0.08 o más negativo al caer)
        // Un salto legítimo empieza positivo y va desacelerando. El Fly mantiene una elevación o caída anormalmente constante.
        boolean isFloating = (dy > -0.01 && dy < 0.1) && (previousDy > -0.01 && previousDy < 0.1);
        boolean isIllegalAscent = dy > 0.42 && player.getFallDistance() == 0;

        if (isFloating || isIllegalAscent) {
            event.setCancelled(true);
            player.teleport(from);

            String message = "[BloonAC] " + player.getName() + " falló Fly (Anomalía física detectada)";
            Bukkit.getLogger().info(message);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("bloon.alerts")) {
                    staff.sendMessage("§c" + message);
                }
            }
        } else {
            lastY.put(player.getUniqueId(), dy);
        }
    }
}

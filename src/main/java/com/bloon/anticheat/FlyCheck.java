package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
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

        // Si está en el suelo, líquidos o rodeado de bloques cercanos (esquinas / paredes), permitimos el movimiento para evitar falsos positivos
        boolean nearBlocks = !player.getLocation().getBlock().getRelative(BlockFace.DOWN).isEmpty() ||
                             !player.getLocation().getBlock().getRelative(BlockFace.NORTH).isEmpty() ||
                             !player.getLocation().getBlock().getRelative(BlockFace.SOUTH).isEmpty() ||
                             !player.getLocation().getBlock().getRelative(BlockFace.EAST).isEmpty() ||
                             !player.getLocation().getBlock().getRelative(BlockFace.WEST).isEmpty();

        if (player.isOnGround() || player.getLocation().getBlock().isLiquid() || (nearBlocks && to.getY() <= from.getY())) {
            lastY.put(player.getUniqueId(), to.getY());
            return;
        }

        double dy = to.getY() - from.getY();
        double previousDy = lastY.getOrDefault(player.getUniqueId(), 0.0);
        
        // Detección estricta de vuelo estático sin colisión lateral legítima
        boolean isFloating = (dy > -0.02 && dy < 0.08) && (previousDy > -0.02 && previousDy < 0.08) && !nearBlocks;
        boolean isIllegalAscent = dy > 0.42 && player.getFallDistance() == 0;

        if (isFloating || isIllegalAscent) {
            event.setCancelled(true);
            player.teleport(from);

            String message = "[BloonAC] " + player.getName() + " falló Fly (Anomalía en aire/esquina)";
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

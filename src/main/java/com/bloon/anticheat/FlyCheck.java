package com.bloon.anticheat;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FlyCheck implements Listener {

    private final Main plugin;

    public FlyCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.fly.enabled", true)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double deltaY = to.getY() - from.getY();

        if (deltaY <= 0) return;
        if (isNearSafeBlock(player.getLocation())) return;

        // Si intenta volar o flotar de forma ilegal
        if (!player.isOnGround() && deltaY > 0.45) {
            // 1. ACCIÓN PRINCIPAL: Hace retroceder al jugador a su posición anterior legítima (Anula el cheat)
            event.setTo(from);
            
            // 2. Notificar al staff mediante el sistema de alertas
            AlertsUtil.sendAlert(player, "Fly / Vuelo Ilegal", "Movimiento vertical bloqueado (DeltaY: " + String.format("%.2f", deltaY) + ")");
        }
    }

    private boolean isNearSafeBlock(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block block = loc.getWorld().getBlockAt(x + dx, y + dy, z + dz);
                    Material type = block.getType();
                    String typeName = type.name();
                    if (typeName.contains("WATER") || 
                        typeName.contains("LAVA") || 
                        typeName.contains("CLIMBABLE") || 
                        typeName.contains("LADDER") || 
                        typeName.contains("VINE") || 
                        typeName.contains("COBWEB") || 
                        typeName.contains("SLIME")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

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

        // Si está en creativo, espectador o tiene permiso de bypass, se ignora
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.fly.enabled", true)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double deltaY = to.getY() - from.getY();

        // Ignoramos si el jugador está cayendo o quieto en el mismo nivel
        if (deltaY <= 0) return;

        // Si está cerca de bloques seguros (agua, escaleras, losas, andamios, etc.)
        if (isNearSafeBlock(player.getLocation())) return;

        // Detección estricta: si no está en el suelo y sube más de lo que permite un salto normal en supervivencia (> 0.42)
        if (!player.isOnGround() && deltaY > 0.419999999999998) {
            
            // 1. ROLLBACK: Devuelve al jugador a la ubicación anterior legítima de forma instantánea
            event.setTo(from);

            // 2. Envía la alerta al staff en el juego y a la consola del hosting
            AlertsUtil.sendAlert(player, "Fly / Vuelo Ilegal", "Vuelo bloqueado (DeltaY: " + String.format("%.3f", deltaY) + ")");
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
                        typeName.contains("SLIME") ||
                        typeName.contains("SCAFFOLDING") ||
                        typeName.contains("SLAB") ||
                        typeName.contains("STAIRS")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

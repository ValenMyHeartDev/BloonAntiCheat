package com.bloon.anticheat;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.fly.enabled", true)) return;

        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        // Guardar la posición segura cuando está firmemente en el suelo
        if (player.isOnGround()) {
            data.setLastSafeLocation(player.getLocation());
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double deltaY = to.getY() - from.getY();

        // Ignorar caídas o movimientos hacia abajo
        if (deltaY <= 0) return;
        if (isNearSafeBlock(player.getLocation())) return;

        // El salto normal de Minecraft en bloque plano tiene un deltaY inicial de ~0.42. 
        // Subimos el umbral a 0.435 y validamos que no tenga efectos de salto (Jump Boost), 
        // permitiendo saltar con total normalidad pero atrapando el vuelo/flotación sostenida.
        double maxJumpHeight = 0.435;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) {
            maxJumpHeight = 0.75; // Margen si tiene poción de salto
        }

        if (!player.isOnGround() && deltaY > maxJumpHeight) {
            event.setCancelled(true);

            if (data.getLastSafeLocation() != null) {
                player.teleport(data.getLastSafeLocation());
            } else {
                player.teleport(from);
            }

            AlertsUtil.sendAlert(player, "Fly / Vuelo Ilegal", "Movimiento bloqueado (DeltaY: " + String.format("%.3f", deltaY) + ")");
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

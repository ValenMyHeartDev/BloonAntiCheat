package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoFallCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public NoFallCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (CheckManager.isIgnored(player)) return;
        if (player.getAllowFlight() || player.isInsideVehicle() || player.isGliding() || player.isSwimming()) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.nofall.enabled", true)) return;

        double fallDistance = player.getFallDistance();
        double minFall = config.getDouble("checks.movement.nofall.min-fall-distance", 3.0);

        boolean isWater = player.getLocation().getBlock().getType().name().contains("WATER") || 
                          player.getLocation().getBlock().getType().name().contains("LAVA");

        if (fallDistance > minFall && player.isOnGround() && !isWater) {
            if (config.getBoolean("checks.movement.nofall.force-damage", true)) {
                player.damage(fallDistance / 2.5);
            }

            UUID uuid = player.getUniqueId();
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            int maxVL = config.getInt("checks.movement.nofall.max-violations", 5);
            if (currentVL >= maxVL) {
                String cmd = config.getString("checks.movement.nofall.punishment-command", "bloon banear %player%");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                violationsMap.put(uuid, 0);
            }
        }
    }
}

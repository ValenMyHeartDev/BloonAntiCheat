package com.bloon.anticheat;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerAndInteractionCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> lastPlaceTime = new HashMap<>();
    private final HashMap<UUID, Long> lastBreakTime = new HashMap<>();

    public PlayerAndInteractionCheck(Main plugin) {
        this.plugin = plugin;
    }

    // Detección de FastPlace / Scaffold
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.player.fastplace", true)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastPlaceTime.getOrDefault(uuid, 0L);
        long diff = now - last;
        lastPlaceTime.put(uuid, now);

        // Menos de 50ms entre colocación de bloques es inhumano (FastPlace/Scaffold)
        if (diff > 0 && diff < 50) {
            event.setCancelled(true);
            AlertsUtil.sendAlert(
                player,
                "FastPlace / Scaffold",
                "Colocación ultrarrápida (" + diff + "ms)",
                AlertsUtil.AlertType.V
            );
        }
    }

    // Detección de FastBreak / Nuker
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.player.fastbreak", true)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastBreakTime.getOrDefault(uuid, 0L);
        long diff = now - last;
        lastBreakTime.put(uuid, now);

        // Romper bloques instantáneamente con intervalos menores a 40ms
        if (diff > 0 && diff < 40) {
            event.setCancelled(true);
            AlertsUtil.sendAlert(
                player,
                "FastBreak / Nuker",
                "Ruptura ilícita (" + diff + "ms)",
                AlertsUtil.AlertType.V
            );
        }
    }
}

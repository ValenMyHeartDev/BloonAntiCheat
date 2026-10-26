package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

public class AutoClickerCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> clickCounts = new HashMap<>();
    private final Map<UUID, Long> lastReset = new HashMap<>();

    public AutoClickerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.autoclicker.enabled", true)) return;

        UUID uuid = player.getUniqueId();
        long currentSecond = System.currentTimeMillis() / 1000;

        lastReset.putIfAbsent(uuid, currentSecond);
        clickCounts.putIfAbsent(uuid, 0);

        if (lastReset.get(uuid) != currentSecond) {
            int clicks = clickCounts.get(uuid);
            int maxCps = config.getInt("checks.autoclicker.max-cps", 18);

            if (clicks > maxCps) {
                // Notificar mediante el sistema de alertas centralizado
                AlertsUtil.sendAlert(player, "AutoClicker", "CPS: " + clicks + " (Máx: " + maxCps + ")");
            }

            clickCounts.put(uuid, 1);
            lastReset.put(uuid, currentSecond);
        } else {
            clickCounts.put(uuid, clickCounts.get(uuid) + 1);
        }
    }
}

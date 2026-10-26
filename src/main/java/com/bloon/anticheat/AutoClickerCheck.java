package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AutoClickerCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, List<Long>> clicks = new HashMap<>();

    public AutoClickerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.combat.autoclicker.enabled", true)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> times = clicks.computeIfAbsent(uuid, k -> new ArrayList<>());
        times.add(now);
        times.removeIf(time -> now - time > 1000);

        int cps = times.size();
        int maxCps = config.getInt("checks.combat.autoclicker.max-cps", 16);

        if (cps > maxCps) {
            event.setCancelled(true);
            AlertsUtil.AlertType alertType = (cps >= 22) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player,
                "AutoClicker",
                "CPS excesivos (" + cps + " CPS | Máx: " + maxCps + ")",
                alertType
            );
            times.clear();
        }
    }
}

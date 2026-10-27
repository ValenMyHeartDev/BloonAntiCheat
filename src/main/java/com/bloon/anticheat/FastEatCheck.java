package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FastEatCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> lastConsume = new HashMap<>();
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public FastEatCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (CheckManager.isIgnored(player)) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.player.fasteat.enabled", true)) return;

        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        long last = lastConsume.getOrDefault(uuid, 0L);

        long minConsumeTime = config.getLong("checks.player.fasteat.min-consume-time-ms", 1400);
        long diff = now - last;

        if (last != 0 && diff < minConsumeTime) {
            event.setCancelled(true);
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            String alertMessage = "§c[BloonAC] " + player.getName() + " falló FastEat (Consumo en " + diff + "ms) §8[VL: " + currentVL + "]";

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission(config.getString("settings.alerts-permission", "bloon.alerts"))) {
                    staff.sendMessage(alertMessage);
                }
            }

            int maxVL = config.getInt("checks.player.fasteat.max-violations", 5);
            if (currentVL >= maxVL) {
                String cmd = config.getString("checks.player.fasteat.punishment-command", "bloon banear %player%");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                violationsMap.put(uuid, 0);
            }
        }

        lastConsume.put(uuid, now);
    }
}

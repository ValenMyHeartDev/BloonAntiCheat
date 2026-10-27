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

public class TimerCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> lastMoveTime = new HashMap<>();
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public TimerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (CheckManager.isIgnored(player)) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.player.timer.enabled", true)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (lastMoveTime.containsKey(uuid)) {
            long timeDiff = now - lastMoveTime.get(uuid);

            // Minecraft normalmente envía paquetes de movimiento cada 50ms (20 ticks/seg).
            // Si el cliente envía paquetes a intervalos menores a 35ms de forma constante, está modificando el Timer.
            if (timeDiff < 35 && timeDiff > 0) {
                int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
                violationsMap.put(uuid, currentVL);

                String alertsPerm = config.getString("settings.alerts-permission", "bloon.alerts");
                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission(alertsPerm)) {
                        staff.sendMessage("§c[BloonAC] " + player.getName() + " falló Timer (Frecuencia: " + timeDiff + "ms) §8[VL: " + currentVL + "]");
                    }
                }

                int maxVL = config.getInt("checks.player.timer.max-violations", 8);
                if (currentVL >= maxVL) {
                    String cmd = config.getString("checks.player.timer.punishment-command", "bloon banear %player%");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                    violationsMap.put(uuid, 0);
                }
            } else {
                if (violationsMap.containsKey(uuid)) {
                    int currentVL = violationsMap.get(uuid);
                    if (currentVL > 0) violationsMap.put(uuid, currentVL - 1);
                }
            }
        }

        lastMoveTime.put(uuid, now);
    }
}

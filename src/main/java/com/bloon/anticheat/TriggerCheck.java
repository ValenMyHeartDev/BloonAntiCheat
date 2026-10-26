package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.UUID;

public class TriggerCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> lastAttackTime = new HashMap<>();
    private final HashMap<UUID, Integer> triggerBuffer = new HashMap<>();

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        boolean triggerBotEnabled = config.getBoolean("checks.TriggerBot.enabled", true) || 
                                    config.getBoolean("punishments.triggerbot.enabled", true);

        if (!triggerBotEnabled) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastAttackTime.getOrDefault(uuid, 0L);
        long diff = now - last;
        lastAttackTime.put(uuid, now);

        int buffer = triggerBuffer.getOrDefault(uuid, 0);

        // Si el intervalo entre golpes es inferior a 130ms de forma constante (velocidad robótica)
        if (diff < 130 && diff > 0) {
            buffer += 2;
        } else if (diff >= 130 && diff < 250) {
            buffer += 1;
        } else {
            buffer = Math.max(0, buffer - 2);
        }

        triggerBuffer.put(uuid, buffer);

        int threshold = 5;

        if (buffer >= threshold) {
            event.setCancelled(true);

            AlertsUtil.AlertType alertType = (diff < 70) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player, 
                "TriggerBot", 
                "Ataque automatizado (Intervalo: " + diff + "ms | Buffer: " + buffer + ")", 
                alertType
            );

            triggerBuffer.put(uuid, 2);
        }
    }
}

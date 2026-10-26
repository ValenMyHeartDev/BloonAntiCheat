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

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
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

        // Umbral ultrasensible: Cancelación inmediata ante cualquier intervalo inferior a 230ms
        if (diff > 0 && diff < 230) {
            event.setCancelled(true);

            // Si el golpe ocurre en menos de 80ms es un TriggerBot/Macro descarado -> [V]
            AlertsUtil.AlertType alertType = (diff < 80) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player, 
                "TriggerBot", 
                "Ataque robótico bloqueado (Intervalo: " + diff + "ms)", 
                alertType
            );
        }
    }
}

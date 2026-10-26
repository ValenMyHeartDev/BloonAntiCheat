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
    public void onDamage(EntityDamageByEntityEntityEvent event) { // O EntityDamageByEntityEvent según tu imports
        // Nota: Asegúrate de importar org.bukkit.event.entity.EntityDamageByEntityEvent
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

        // Si el intervalo entre golpes es inferior a 130ms de forma constante (velocidad robótica/macro)
        if (diff < 130 && diff > 0) {
            buffer += 2;
        } else if (diff >= 130 && diff < 250) {
            buffer += 1; // Ritmo muy rápido sospechoso
        } else {
            // Si el jugador golpea a ritmo normal humano, reducimos el búfer
            buffer = Math.max(0, buffer - 2);
        }

        triggerBuffer.put(uuid, buffer);

        // Umbral de activación bajo para que salte rápido (puedes ajustarlo si deseas)
        int threshold = 5;

        if (buffer >= threshold) {
            event.setCancelled(true);

            // Asignamos nivel V si el intervalo es inferior a 70ms (extremadamente inhumano), sino P
            AlertsUtil.AlertType alertType = (diff < 70) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player, 
                "TriggerBot", 
                "Ataque automatizado (Intervalo: " + diff + "ms | Buffer: " + buffer + ")", 
                alertType
            );

            // Reseteamos parcialmente el búfer
            triggerBuffer.put(uuid, 2);
        }
    }
}

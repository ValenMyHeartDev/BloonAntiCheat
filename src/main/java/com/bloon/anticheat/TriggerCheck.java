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

        // Sensibilidad aumentada: Margen de milisegundos más amplio y mayor ganancia de búfer
        if (diff < 180 && diff > 0) {
            buffer += 3; // Sube mucho más rápido ante cualquier patrón constante
        } else if (diff >= 180 && diff < 300) {
            buffer += 1;
        } else {
            // Decaimiento lento para retener la sospecha en tramposos
            buffer = Math.max(0, buffer - 1);
        }

        triggerBuffer.put(uuid, buffer);

        // Umbral ultrabajo: Con apenas 3 puntos acumulados salta la alerta y el bloqueo
        int threshold = 3;

        if (buffer >= threshold) {
            event.setCancelled(true);

            // Alerta inmediata de nivel V si es muy rápido, o P
            AlertsUtil.AlertType alertType = (diff < 90) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player, 
                "TriggerBot", 
                "Ataque automatizado (Intervalo: " + diff + "ms | Buffer: " + buffer + ")", 
                alertType
            );

            // Reseteo parcial para continuar frenando golpes consecutivos
            triggerBuffer.put(uuid, 1);
        }
    }
}

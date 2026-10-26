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

        // Umbral de alta sensibilidad: Cualquier golpe con un intervalo menor a 220ms será interceptado de inmediato
        // (Un humano jugando limpio rara vez baja de 250-300ms constantes en cada golpe de forma perfecta)
        if (diff > 0 && diff < 220) {
            event.setCancelled(true);

            // Clasificación automática de nivel según la velocidad inhumana
            AlertsUtil.AlertType alertType = (diff < 100) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            // Envía la alerta obligatoria a consola y staff con los niveles F/P/V configurados
            AlertsUtil.sendAlert(
                player, 
                "TriggerBot", 
                "Ataque automatizado bloqueado (Intervalo: " + diff + "ms)", 
                alertType
            );
        }
    }
}

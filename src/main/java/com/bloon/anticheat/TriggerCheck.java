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
    private final HashMap<UUID, Integer> rapidHitsCounter = new HashMap<>();

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("punishments.triggerbot.enabled", true) && 
            !config.getBoolean("checks.TriggerBot.enabled", true)) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lastTime = lastAttackTime.getOrDefault(player.getUniqueId(), 0L);
        long diff = currentTime - lastTime;
        lastAttackTime.put(player.getUniqueId(), currentTime);

        // Si el jugador tarda más de 600ms en dar el siguiente golpe, se resetea el contador (PvP normal)
        if (diff > 600) {
            rapidHitsCounter.put(player.getUniqueId(), 0);
            return;
        }

        // Un TriggerBot o AutoClicker ataca a intervalos extremadamente cortos y automatizados (< 90ms)
        if (diff < 90) {
            int count = rapidHitsCounter.getOrDefault(player.getUniqueId(), 0) + 1;
            rapidHitsCounter.put(player.getUniqueId(), count);

            // Con apenas 3 golpes consecutivos a velocidad de máquina, bloquea el ataque y avisa al instante
            if (count >= 3) {
                event.setCancelled(true);

                AlertsUtil.sendAlert(player, "TriggerBot", "Ataque automatizado bloqueado (Intervalo: " + diff + "ms)");
                
                // Reiniciamos el contador tras la sanción
                rapidHitsCounter.put(player.getUniqueId(), 0);
            }
        } else {
            // Si el intervalo es humano (>= 90ms), reducimos el contador de sospecha gradualmente
            int count = rapidHitsCounter.getOrDefault(player.getUniqueId(), 0);
            if (count > 0) {
                rapidHitsCounter.put(player.getUniqueId(), count - 1);
            }
        }
    }
}

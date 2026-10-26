package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TriggerCheck implements Listener {

    private final Main plugin;

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

        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null) return;

        // Aumentamos la sensibilidad sumando peso por cada clic de combate
        data.addTriggerBuffer(2.5); 
        double currentBuffer = data.getTriggerBuffer();

        // Umbral más bajo para una detección mucho más rápida y estricta
        double threshold = config.getDouble("punishments.triggerbot.buffer-threshold", 6.0);

        if (currentBuffer >= threshold) {
            // Cancela el daño y el golpe para que no tenga efecto
            event.setCancelled(true);

            AlertsUtil.sendAlert(player, "TriggerBot", "Ataque sospechoso bloqueado (Buffer: " + String.format("%.1f", currentBuffer) + ")");

            // Reiniciar el buffer tras la sanción
            data.setTriggerBuffer(0.0);
        }
    }
}

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

        data.addTriggerBuffer(1.0);
        double currentBuffer = data.getTriggerBuffer();

        double threshold = config.getDouble("punishments.triggerbot.buffer-threshold", 10.0);

        // Si supera el límite de sospecha
        if (currentBuffer >= threshold) {
            // 1. CANCELAR EL GOLPE (El golpe no contará ni infligirá daño)
            event.setCancelled(true);

            AlertsUtil.sendAlert(player, "TriggerBot", "Golpe bloqueado (Buffer: " + currentBuffer + ")");

            // Reiniciamos o reducimos el buffer tras anular el golpe
            data.setTriggerBuffer(0.0);
        }
    }
}

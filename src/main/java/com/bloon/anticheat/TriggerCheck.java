package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TriggerCheck implements Listener {

    private final Main plugin;

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("punishments.triggerbot.enabled", true) && 
            !config.getBoolean("checks.TriggerBot.enabled", true)) {
            return;
        }

        // Obtenemos los datos del jugador desde el almacenamiento principal
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null) return;

        // Incrementamos el buffer de combate
        data.addTriggerBuffer(1.0);
        double currentBuffer = data.getTriggerBuffer();

        double threshold = config.getDouble("punishments.triggerbot.buffer-threshold", 10.0);

        // Si el jugador supera el límite de ataques sospechosos
        if (currentBuffer >= threshold) {
            AlertsUtil.sendAlert(player, "TriggerBot", "Buffer acumulado: " + currentBuffer);
            
            // Reiniciamos el buffer tras emitir la alerta
            data.setTriggerBuffer(0.0);
        }
    }
}

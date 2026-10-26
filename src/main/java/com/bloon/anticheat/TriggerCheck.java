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

        // Obtenemos o creamos los datos en memoria del jugador
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        
        // Incrementamos el buffer por cada ataque registrado
        double currentBuffer = data.getTriggerBuffer() + 1.0;
        data.setTriggerBuffer(currentBuffer);

        double threshold = config.getDouble("punishments.triggerbot.buffer-threshold", 10.0);

        // Si el jugador supera el límite de ataques sospechosos
        if (currentBuffer >= threshold) {
            AlertsUtil.sendAlert(player, "TriggerBot", "Buffer acumulado: " + currentBuffer);
            
            // Opcional: Reiniciar buffer tras la alerta o aplicar sanción configurada
            data.setTriggerBuffer(0.0);
        }
    }
}

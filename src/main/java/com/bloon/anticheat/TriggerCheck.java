package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class TriggerCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, List<Long>> attackTimestamps = new HashMap<>();

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        boolean triggerBotEnabled = config.getBoolean("checks.TriggerBot.enabled", true) || 
                                    config.getBoolean("punishments.triggerbot.enabled", true);

        if (!triggerBotEnabled) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> timestamps = attackTimestamps.computeIfAbsent(uuid, k -> new ArrayList<>());
        timestamps.add(now);

        // Limpiamos los ataques de hace más de 1 segundo
        timestamps.removeIf(time -> now - time > 1000);

        int attacksInOneSecond = timestamps.size();
        
        // Un TriggerBot o AutoClicker suele superar los 15-18 ataques por segundo de forma sostenida
        int maxAllowed = config.getInt("checks.autoclicker.max-cps", 18);

        if (attacksInOneSecond > maxAllowed) {
            event.setCancelled(true);

            // Asignamos el nivel V (Confirmado) si es exagerado, o P (Posiblemente)
            AlertsUtil.AlertType alertType = (attacksInOneSecond >= 24) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.P;

            AlertsUtil.sendAlert(
                player, 
                "TriggerBot / AutoClicker", 
                "Ataques muy rápidos (" + attacksInOneSecond + " CPS)", 
                alertType
            );

            timestamps.clear();
        }
    }
}

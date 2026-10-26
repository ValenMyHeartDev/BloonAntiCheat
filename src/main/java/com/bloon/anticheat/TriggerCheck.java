package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class TriggerCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, List<Long>> clickTimestamps = new HashMap<>();

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        
        // Verificamos si el autoclicker o triggerbot están habilitados en la config
        boolean autoClickerEnabled = config.getBoolean("checks.autoclicker.enabled", true);
        boolean triggerBotEnabled = config.getBoolean("checks.TriggerBot.enabled", true) || 
                                    config.getBoolean("punishments.triggerbot.enabled", true);

        if (!autoClickerEnabled && !triggerBotEnabled) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> timestamps = clickTimestamps.computeIfAbsent(uuid, k -> new ArrayList<>());
        timestamps.add(now);

        // Limpiamos los clics de hace más de 1 segundo para obtener los CPS reales exactos
        timestamps.removeIf(time -> now - time > 1000);

        int currentCPS = timestamps.size();
        
        // Obtenemos el límite máximo de CPS definido en tu config.yml (por defecto 18)
        int maxAllowedCPS = config.getInt("checks.autoclicker.max-cps", 18);

        if (currentCPS > maxAllowedCPS) {
            event.setCancelled(true);

            AlertsUtil.sendAlert(player, "AutoClicker / TriggerBot", "CPS excesivos detectados (" + currentCPS + " / " + maxAllowedCPS + " máx)");

            // Limpiamos parcialmente la lista para evitar bucles de alertas masivas en el mismo segundo
            timestamps.clear();
        }
    }
}

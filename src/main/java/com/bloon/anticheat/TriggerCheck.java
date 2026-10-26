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
    // Guardamos la lista de marcas de tiempo (milisegundos) de los últimos clics de cada jugador
    private final HashMap<UUID, List<Long>> clickTimestamps = new HashMap<>();

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        // Solo nos interesan los clics izquierdos (intentos de golpear/atacar al aire o bloques)
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("punishments.triggerbot.enabled", true) && 
            !config.getBoolean("checks.TriggerBot.enabled", true)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> timestamps = clickTimestamps.computeIfAbsent(uuid, k -> new ArrayList<>());

        // Añadir el clic actual
        timestamps.add(now);

        // Limpiar clics más antiguos de 1 segundo (1000 ms) para medir los CPS reales
        timestamps.removeIf(time -> now - time > 1000);

        // Si el jugador supera los 15 clics por segundo (CPS inhumano para juego manual sostenido)
        int currentCPS = timestamps.size();
        int maxAllowedCPS = config.getInt("checks.autoclicker.max-cps", 16);

        if (currentCPS > maxAllowedCPS) {
            // Cancelar la acción para evitar la ventaja
            event.setCancelled(true);

            AlertsUtil.sendAlert(player, "TriggerBot / AutoClicker", "CPS excesivos detectados (" + currentCPS + " CPS)");

            // Limpiar la lista parcialmente para evitar spam consecutivo de alertas
            timestamps.clear();
        }
    }
}

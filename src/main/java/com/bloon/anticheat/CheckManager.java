package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CheckManager {

    private final Main plugin;
    private static final Set<UUID> ignoredPlayers = new HashSet<>();

    public CheckManager(Main plugin) {
        this.plugin = plugin;
    }

    public void registerAllChecks() {
        PluginManager pm = Bukkit.getPluginManager();

        // Módulos de Movimiento y Combate
        pm.registerEvents(new FlyCheck(plugin), plugin);
        pm.registerEvents(new ClickTPCheck(plugin), plugin);
        pm.registerEvents(new CriticalsCheck(plugin), plugin);
        pm.registerEvents(new TriggerCheck(plugin), plugin);
        pm.registerEvents(new ReachCheck(plugin), plugin);
        pm.registerEvents(new PvPCombatCheck(plugin), plugin);

        plugin.getLogger().info("[BloonAC] ¡Todos los módulos de seguridad cargados exitosamente!");
    }

    // Alterna el estado de ignorado para un jugador
    public static boolean toggleIgnore(UUID uuid) {
        if (ignoredPlayers.contains(uuid)) {
            ignoredPlayers.remove(uuid);
            return false; // Ya no está ignorado (vuelve a ser detectado)
        } else {
            ignoredPlayers.add(uuid);
            return true;  // Ahora está ignorado (puede usar hacks libremente)
        }
    }

    // Método que evalúa si un jugador debe ser ignorado por completo por el anticheat
    public static boolean isIgnored(Player player) {
        return ignoredPlayers.contains(player.getUniqueId());
    }
}

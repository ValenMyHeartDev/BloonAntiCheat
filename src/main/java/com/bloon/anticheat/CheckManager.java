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

        // --- Módulos de Movimiento ---
        pm.registerEvents(new FlyCheck(plugin), plugin);
        pm.registerEvents(new ClickTPCheck(plugin), plugin);
        pm.registerEvents(new SpeedCheck(plugin), plugin);
        pm.registerEvents(new NoFallCheck(plugin), plugin);
        pm.registerEvents(new JesusCheck(plugin), plugin);

        // --- Módulos de Combate ---
        pm.registerEvents(new CriticalsCheck(plugin), plugin);
        pm.registerEvents(new TriggerCheck(plugin), plugin);
        pm.registerEvents(new ReachCheck(plugin), plugin);
        pm.registerEvents(new PvPCombatCheck(plugin), plugin);
        pm.registerEvents(new KillAuraCheck(plugin), plugin);

        // --- Módulos de Jugador y Paquetes ---
        pm.registerEvents(new FastEatCheck(plugin), plugin);
        pm.registerEvents(new TimerCheck(plugin), plugin);

        plugin.getLogger().info("[BloonAC] ¡Todos los módulos de seguridad cargados exitosamente!");
    }

    // Alterna el estado de ignorado para un jugador (/bloon ignore)
    public static boolean toggleIgnore(UUID uuid) {
        if (ignoredPlayers.contains(uuid)) {
            ignoredPlayers.remove(uuid);
            return false; // Vuelve a ser detectado
        } else {
            ignoredPlayers.add(uuid);
            return true;  // Inmune a detecciones para pruebas
        }
    }

    // Retorna true si el jugador está siendo ignorado
    public static boolean isIgnored(Player player) {
        return ignoredPlayers.contains(player.getUniqueId());
    }
}

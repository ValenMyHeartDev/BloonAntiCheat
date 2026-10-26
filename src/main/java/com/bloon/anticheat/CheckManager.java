package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class CheckManager {

    private final Main plugin;

    public CheckManager(Main plugin) {
        this.plugin = plugin;
    }

    public void registerAllChecks() {
        PluginManager pm = Bukkit.getPluginManager();

        // Combate y PvP Moderno
        pm.registerEvents(new TriggerCheck(plugin), plugin);
        pm.registerEvents(new ReachCheck(plugin), plugin);
        pm.registerEvents(new PvPCombatCheck(plugin), plugin);
        pm.registerEvents(new AutoClickerCheck(plugin), plugin);

        // Movimiento y Física
        pm.registerEvents(new MovementAndExploitCheck(plugin), plugin);

        // Jugador e Interacción
        pm.registerEvents(new PlayerAndInteractionCheck(plugin), plugin);

        // Seguridades del Servidor y Exploits
        pm.registerEvents(new ExploitCheck(plugin), plugin);

        plugin.getLogger().info("[BloonAC] ¡Todos los módulos del Anticheat han sido registrados exitosamente!");
    }
}

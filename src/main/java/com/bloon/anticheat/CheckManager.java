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

        // Módulos de Movimiento y Exploits
        pm.registerEvents(new FlyCheck(plugin), plugin);
        pm.registerEvents(new ClickTPCheck(plugin), plugin);

        // Módulos de Combate
        pm.registerEvents(new TriggerCheck(plugin), plugin);
        pm.registerEvents(new ReachCheck(plugin), plugin);
        pm.registerEvents(new PvPCombatCheck(plugin), plugin);

        plugin.getLogger().info("[BloonAC] ¡Módulos registrados correctamente!");
    }
}

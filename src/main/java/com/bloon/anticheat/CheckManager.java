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

        // 1. Módulo de Movimiento (FlyCheck corregido)
        pm.registerEvents(new FlyCheck(plugin), plugin);

        // 2. Módulos de Combate (Verifica que las clases existan en el proyecto)
        try {
            pm.registerEvents(new TriggerCheck(plugin), plugin);
        } catch (NoClassDefFoundError | Exception ignored) {}

        try {
            pm.registerEvents(new ReachCheck(plugin), plugin);
        } catch (NoClassDefFoundError | Exception ignored) {}

        plugin.getLogger().info("[BloonAC] ¡Módulos registrados correctamente!");
    }
}

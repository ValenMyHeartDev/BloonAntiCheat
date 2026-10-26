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

        // 1. Registro seguro de Movimiento (FlyCheck optimizado)
        pm.registerEvents(new FlyCheck(plugin), plugin);

        // 2. Registro seguro de Combate básico
        pm.registerEvents(new TriggerCheck(plugin), plugin);
        pm.registerEvents(new ReachCheck(plugin), plugin);
        pm.registerEvents(new PvPCombatCheck(plugin), plugin);

        // Si ya creaste CriticalsCheck.java, descomenta la siguiente línea:
        // pm.registerEvents(new CriticalsCheck(plugin), plugin);

        plugin.getLogger().info("[BloonAC] ¡Módulos de seguridad cargados exitosamente!");
    }
}

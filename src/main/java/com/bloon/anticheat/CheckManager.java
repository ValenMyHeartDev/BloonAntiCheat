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

        // Módulos de Combate
        pm.registerEvents(new TriggerCheck(plugin), plugin);
        pm.registerEvents(new ReachCheck(plugin), plugin);
        pm.registerEvents(new PvPCombatCheck(plugin), plugin);
        pm.registerEvents(new AutoClickerCheck(plugin), plugin);

        // Módulos de Movimiento
        pm.registerEvents(new FlyCheck(plugin), plugin);
        pm.registerEvents(new MovementAndExploitCheck(plugin), plugin);

        // Interacción y Seguridad
        pm.registerEvents(new PlayerAndInteractionCheck(plugin), plugin);
        pm.registerEvents(new ExploitCheck(plugin), plugin);

        plugin.getLogger().info("[BloonAC] ¡Todos los módulos han sido cargados exitosamente!");
    }
}

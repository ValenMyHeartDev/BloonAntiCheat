package com.bloon.anticheat;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Guardar config por defecto
        saveDefaultConfig();

        // Registrar comando /bloon
        if (getCommand("bloon") != null) {
            BloonCommand bloonCmd = new BloonCommand(this);
            getCommand("bloon").setExecutor(bloonCmd);
            getCommand("bloon").setTabCompleter(bloonCmd);
        }

        // Cargar todos los módulos mediante CheckManager
        new CheckManager(this).registerAllChecks();

        getLogger().info("[BloonAC] Plugin activado e inicializado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[BloonAC] Plugin desactivado.");
    }
}

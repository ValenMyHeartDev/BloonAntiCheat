package com.bloon.anticheat;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // 1. Guardar la configuración por defecto (config.yml)
        saveDefaultConfig();

        // 2. Registrar el comando /bloon y su ejecutor/tab completer
        if (getCommand("bloon") != null) {
            BloonCommand bloonCmd = new BloonCommand(this);
            getCommand("bloon").setExecutor(bloonCmd);
            getCommand("bloon").setTabCompleter(bloonCmd);
        }

        // 3. Inicializar y registrar todos los checks de eventos mediante CheckManager
        new CheckManager(this).registerAllChecks();

        getLogger().info("[BloonAC] Plugin activado e inicializado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[BloonAC] Plugin desactivado correctamente.");
    }
}

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

        // 3. Inicializar y registrar todos los checks mediante CheckManager
        new CheckManager(this).registerAllChecks();

        // 4. Registrar los eventos de los Menús GUI pasando 'this' (Main)
        getServer().getPluginManager().registerEvents(new BloonGUIListener(this), this);

        // 5. Registrar el Listener para el control de chat de jugadores muteados
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        getLogger().info("[BloonAC] Plugin activado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[BloonAC] Plugin desactivado correctamente.");
    }
}

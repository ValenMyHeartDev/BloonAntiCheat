package com.bloon.anticheat;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // 1. Guardar config.yml por defecto si no existe
        saveDefaultConfig();

        // 2. Inicializar base de datos SQLite para historiales y sanciones
        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        // 3. Registrar todos los Checks de movimiento y combate
        CheckManager checkManager = new CheckManager(this);
        checkManager.registerAllChecks();

        // 4. Registrar Comandos y TabCompleter
        if (getCommand("bloon") != null) {
            BloonCommand bloonCmd = new BloonCommand(this);
            getCommand("bloon").setExecutor(bloonCmd);
            getCommand("bloon").setTabCompleter(bloonCmd);
        }

        // 5. Registrar Listeners de la GUI y control de IPs en el servidor
        getServer().getPluginManager().registerEvents(new BloonGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(), this);

        getLogger().info("[BloonAC] ¡Bloon AntiCheat v1.0 activado con éxito y listo para producción!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[BloonAC] Desactivando BloonAC y guardando datos...");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}

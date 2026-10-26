package com.bloon.anticheat;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin {

    private static Main instance;
    private final HashMap<UUID, PlayerData> playerDataMap = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. Registrar Comando Principal y TabCompleter
        if (getCommand("bloon") != null) {
            BloonCommand bloonCommand = new BloonCommand(this);
            getCommand("bloon").setExecutor(bloonCommand);
            getCommand("bloon").setTabCompleter(bloonCommand);
        }

        // 2. Registrar TODOS los Listeners de Detección y Menús
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new TriggerCheck(this), this);
        getServer().getPluginManager().registerEvents(new FlyCheck(this), this);
        getServer().getPluginManager().registerEvents(new ReachCheck(this), this);
        getServer().getPluginManager().registerEvents(new AutoClickerCheck(this), this);

        getLogger().info("¡Bloon AntiCheat V1 cargado y activado correctamente con todas sus detecciones!");
    }

    @Override
    public void onDisable() {
        playerDataMap.clear();
        getLogger().info("Bloon AntiCheat V1 desactivado.");
    }

    public static Main getInstance() {
        return instance;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }
}

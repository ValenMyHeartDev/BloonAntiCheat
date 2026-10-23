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

        if (getCommand("bloon") != null) {
            getCommand("bloon").setExecutor(new BloonCommand(this));
        }

        // Registrar los Listeners de eventos (Menú y Detector de Hacks)
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new TriggerCheck(this), this);

        getLogger().info("¡Bloon AntiCheat V1 cargado y activado correctamente con TriggerBot!");
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

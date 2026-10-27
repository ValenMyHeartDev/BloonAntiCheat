package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CheckManager {

    private final Main plugin;
    private static final Set<UUID> ignoredPlayers = new HashSet<>();

    public CheckManager(Main plugin) {
        this.plugin = plugin;
    }

    public void registerAllChecks() {
        PluginManager pm = Bukkit.getPluginManager();

        // --- Módulos de Combate y Jugador Verificados ---
        pm.registerEvents(new CriticalsCheck(plugin), plugin);
        pm.registerEvents(new TriggerCheck(plugin), plugin);
        pm.registerEvents(new ReachCheck(plugin), plugin);
        pm.registerEvents(new KillAuraCheck(plugin), plugin);
        pm.registerEvents(new FastEatCheck(plugin), plugin);

        plugin.getLogger().info("[BloonAC] ¡Todos los módulos de seguridad cargados exitosamente!");
    }

    public static boolean toggleIgnore(UUID uuid) {
        if (ignoredPlayers.contains(uuid)) {
            ignoredPlayers.remove(uuid);
            return false;
        } else {
            ignoredPlayers.add(uuid);
            return true;
        }
    }

    public static boolean isIgnored(Player player) {
        return ignoredPlayers.contains(player.getUniqueId());
    }
}

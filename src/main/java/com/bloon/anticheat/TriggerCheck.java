package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TriggerCheck implements Listener {

    private final Main plugin;

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        // Omitir administradores o jugadores con permiso de bypass
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.trigger.enabled", true)) return;

        // Comprobación de TPS mínimo del servidor para evitar falsos positivos por lag general
        double currentTps = Bukkit.getTPS()[0];
        double minTps = config.getDouble("checks.trigger.min-tps", 18.0);
        if (currentTps < minTps) return;

        // Comprobación de Ping del jugador
        int maxPing = config.getInt("checks.trigger.max-ping", 250);
        if (player.getPing() > maxPing) return;

        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        
        // Aumentar buffer por interacción de combate rápida
        data.addTriggerBuffer(1.0);

        double requiredBuffer = config.getDouble("checks.trigger.required-buffer", 4.0);

        // Si el jugador supera el buffer permitido, se aplica la sanción
        if (data.getTriggerBuffer() >= requiredBuffer) {
            triggerPunishment(player);
            data.resetBuffer();
        } else {
            // Reducir gradualmente el buffer con el tiempo (simulado bajando un poco por golpe limpio)
            data.reduceTriggerBuffer(0.25);
        }
    }

    private void triggerPunishment(Player player) {
        FileConfiguration config = plugin.getConfig();
        String action = config.getString("punishments.default-action", "BAN");
        
        if (action.equalsIgnoreCase("BAN")) {
            String command = config.getString("punishments.commands.ban", "ban %player% Sancionado por TriggerBot")
                    .replace("%player%", player.getName())
                    .replace("%check%", "TriggerBot");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            String command = config.getString("punishments.commands.kick", "kick %player% Comportamiento sospechoso")
                    .replace("%player%", player.getName())
                    .replace("%check%", "TriggerBot");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}

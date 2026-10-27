package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvPCombatCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public PvPCombatCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (CheckManager.isIgnored(player)) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.combat.pvpcombat.enabled", true)) return;

        // Comprobación de combate general / invalidez de objetivo
        if (event.getEntity().isDead()) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            String alertsPerm = config.getString("settings.alerts-permission", "bloon.alerts");
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission(alertsPerm)) {
                    staff.sendMessage("§c[BloonAC] " + player.getName() + " falló PvPCombat (Golpe a objetivo muerto/inválido) §8[VL: " + currentVL + "]");
                }
            }

            int maxVL = config.getInt("checks.combat.pvpcombat.max-violations", 6);
            if (currentVL >= maxVL) {
                String cmd = config.getString("checks.combat.pvpcombat.punishment-command", "bloon banear %player%");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                violationsMap.put(uuid, 0);
            }
        }
    }
}

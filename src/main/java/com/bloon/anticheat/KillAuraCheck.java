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

public class KillAuraCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public KillAuraCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (CheckManager.isIgnored(player)) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.combat.killaura.enabled", true)) return;

        float yawChange = Math.abs(player.getEyeLocation().getYaw() - player.getLocation().getYaw());
        double maxYawRate = config.getDouble("checks.combat.killaura.max-yaw-rate", 175.0);

        if (yawChange > maxYawRate) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            String alertMessage = "§c[BloonAC] " + player.getName() + " falló KillAura (Giro anómalo: " 
                    + String.format("%.1f", yawChange) + "°) §8[VL: " + currentVL + "]";

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission(config.getString("settings.alerts-permission", "bloon.alerts"))) {
                    staff.sendMessage(alertMessage);
                }
            }

            int maxVL = config.getInt("checks.combat.killaura.max-violations", 8);
            if (currentVL >= maxVL) {
                String cmd = config.getString("checks.combat.killaura.punishment-command", "bloon banear %player%");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                violationsMap.put(uuid, 0);
            }
        }
    }
}

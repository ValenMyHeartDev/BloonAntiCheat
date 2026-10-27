package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JesusCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public JesusCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (CheckManager.isIgnored(player)) return;
        if (player.getAllowFlight() || player.isInsideVehicle() || player.isGliding() || player.isSwimming()) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.jesus.enabled", true)) return;

        Location to = event.getTo();
        if (to == null) return;

        Material currentBlock = to.getBlock().getType();
        Material belowBlock = to.clone().subtract(0, 0.1, 0).getBlock().getType();

        boolean isLiquid = currentBlock == Material.WATER || currentBlock == Material.LAVA || 
                           belowBlock == Material.WATER || belowBlock == Material.LAVA;

        if (isLiquid && player.getVelocity().getY() >= -0.1) {
            if (to.getY() % 1.0 == 0.0 || to.getY() % 0.5 == 0.0) {
                if (config.getBoolean("checks.movement.jesus.cancel-water-walk", true)) {
                    event.setCancelled(true);
                    player.teleport(event.getFrom());
                }

                UUID uuid = player.getUniqueId();
                int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
                violationsMap.put(uuid, currentVL);

                String alertMessage = "§c[BloonAC] " + player.getName() + " falló Jesus (Caminar sobre agua/lava) §8[VL: " + currentVL + "]";

                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission(config.getString("settings.alerts-permission", "bloon.alerts"))) {
                        staff.sendMessage(alertMessage);
                    }
                }

                int maxVL = config.getInt("checks.movement.jesus.max-violations", 6);
                if (currentVL >= maxVL) {
                    String cmd = config.getString("checks.movement.jesus.punishment-command", "bloon banear %player%");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                    violationsMap.put(uuid, 0);
                }
            }
        }
    }
}

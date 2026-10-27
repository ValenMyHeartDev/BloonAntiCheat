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

public class TriggerCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (CheckManager.isIgnored(player)) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.combat.triggerbot.enabled", true)) return;

        // Si ataca exactamente en el instante en que la entidad entra en la mira
        UUID uuid = player.getUniqueId();
        int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
        
        if (currentVL > config.getInt("checks.combat.triggerbot.max-violations", 7)) {
            event.setCancelled(true);
            String cmd = config.getString("checks.combat.triggerbot.punishment-command", "bloon banear %player%");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            violationsMap.put(uuid, 0);
        }
    }
}

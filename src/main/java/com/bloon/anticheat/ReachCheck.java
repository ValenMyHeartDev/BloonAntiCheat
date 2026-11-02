package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReachCheck implements Listener {

    private final Main plugin;
    private final Map<UUID, Integer> violationsMap = new HashMap<>();

    public ReachCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        Entity target = event.getEntity();

        if (CheckManager.isIgnored(player)) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.combat.reach.enabled", true)) return;

        double distance = player.getEyeLocation().toVector().setY(0)
                .distance(target.getLocation().toVector().setY(0));
        
        double maxReach = config.getDouble("checks.combat.reach.max-reach-distance", 3.15);

        if (distance > maxReach) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            String alertsPerm = config.getString("settings.alerts-permission", "bloon.alerts");
            String alertMessage = "§c[BloonAC] " + player.getName() + " falló Reach (" 
                    + String.format("%.2f", distance) + "m > " + maxReach + "m) §8[VL: " + currentVL + "]";

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission(alertsPerm)) {
                    staff.sendMessage(alertMessage);
                }
            }

            int maxVL = config.getInt("checks.combat.reach.max-violations", 10);
            if (currentVL >= maxVL) {
                String cmd = config.getString("checks.combat.reach.punishment-command", "bloon banear %player%");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                violationsMap.put(uuid, 0);
            }
        }
    }
}
```[cite: 4]

### Consejo adicional si persiste:
Si tu editor de código (como Eclipse, IntelliJ o VS Code) sigue marcando error en esas líneas específicas, selecciona las líneas 65 a la 75 de tu archivo, bórralas manualmente con la tecla Backspace/Supr y vuelve a escribirlas a mano o usa la opción de pegar como texto plano (*Paste as Plain Text*).

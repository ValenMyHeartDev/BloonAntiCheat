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
            String alertMessage = "§c[BloonAC] " + player.getName() + " fallo Reach (" 
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

---

### 2. `TriggerCheck.java` (Limpio)
```java
package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

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
        Entity target = event.getEntity();

        if (CheckManager.isIgnored(player)) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.combat.triggerbot.enabled", true)) return;

        Vector playerDir = player.getEyeLocation().getDirection().normalize();
        Vector targetDir = target.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize();

        double dotProduct = playerDir.dot(targetDir);
        double minPrecision = config.getDouble("checks.combat.triggerbot.min-precision", 0.35);

        if (dotProduct < minPrecision) {
            UUID uuid = player.getUniqueId();
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            String alert = "§c[BloonAC] " + player.getName() + " fallo TriggerBot §8[VL: " + currentVL + "]";
            
            String alertsPerm = config.getString("settings.alerts-permission", "bloon.alerts");
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission(alertsPerm)) {
                    staff.sendMessage(alert);
                }
            }

            int maxVL = config.getInt("checks.combat.triggerbot.max-violations", 7);
            if (currentVL >= maxVL) {
                String cmd = config.getString("checks.combat.triggerbot.punishment-command", "bloon banear %player%");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                violationsMap.put(uuid, 0);
            }
        }
    }
}
```[cite: 3]

*Nota:* Asegúrate de guardar ambos archivos utilizando codificación **UTF-8** en tu editor de código para que los símbolos de sección (`§`) y los acentos no generen conflictos de compilación.

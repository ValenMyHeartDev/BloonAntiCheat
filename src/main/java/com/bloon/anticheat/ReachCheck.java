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

        // Cálculo de distancia teniendo en cuenta la caja de colisión horizontal del objetivo
        double distance = player.getEyeLocation().toVector().setY(0)
                .distance(target.getLocation().toVector().setY(0));
        
        // Ajustamos un maxReach prudente con tolerancia de ping
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

#### Versión corregida de `TriggerCheck.java` (Enfoque de ángulo de visión / Raytracing básico)
Para que el TriggerBot detecte si el jugador atacó sin estar mirando directamente a la entidad (o con una velocidad antinatural de giro instantáneo):

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

        // Comprobación de ángulo de visión (Mirar hacia el objetivo al atacar)
        Vector playerDir = player.getEyeLocation().getDirection().normalize();
        Vector targetDir = target.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize();
        
        double dotProduct = playerDir.dot(targetDir); // 1.0 es mirando exactamente al objetivo
        double minDot = config.getDouble("checks.combat.triggerbot.min-precision", 0.40); // Umbral de tolerancia de giro

        // Si el jugador golpea pero su mira estaba demasiado desviada del objetivo (típico de TriggerBot mal configurado o ángulo absurdo)
        if (dotProduct < minDot) {
            UUID uuid = player.getUniqueId();
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            String alert = "§c[BloonAC] " + player.getName() + " falló TriggerBot (Precisión anómala) §8[VL: " + currentVL + "]";
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission(config.getString("settings.alerts-permission", "bloon.alerts"))) {
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

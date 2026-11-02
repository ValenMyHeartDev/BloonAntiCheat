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

        // Vector de la dirección de la vista del jugador
        Vector playerDir = player.getEyeLocation().getDirection().normalize();
        // Vector desde los ojos del jugador hacia la posición del objetivo
        Vector targetDir = target.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize();

        // Producto escalar (Dot Product): mide qué tan alineada está la mira con el objetivo (1.0 es centro exacto)
        double dotProduct = playerDir.dot(targetDir);
        double minPrecision = config.getDouble("checks.combat.triggerbot.min-precision", 0.35);

        // Si el jugador asesta un golpe pero su ángulo de visión estaba demasiado desviado del objetivo
        if (dotProduct < minPrecision) {
            UUID uuid = player.getUniqueId();
            int currentVL = violationsMap.getOrDefault(uuid, 0) + 1;
            violationsMap.put(uuid, currentVL);

            String alert = "§c[BloonAC] " + player.getName() + " falló TriggerBot (Ángulo anómalo: " 
                    + String.format("%.2f", dotProduct) + ") §8[VL: " + currentVL + "]";
            
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

### ¿Qué se ha corregido en esta versión?
1. **Validación de Vector y Ángulo:** Se calcula el producto escalar (`dotProduct`) entre la dirección a donde mira el usuario y la posición real del objetivo[cite: 3]. Esto detecta si un cliente envía clics automáticos sin estar mirando al contrincante.
2. **Parámetro de configuración limpio:** Utiliza `checks.combat.triggerbot.min-precision` para definir el umbral de tolerancia. Si lo configuras muy bajo (ej. `0.2`), tolerará giros amplios; si lo pones más alto (ej. `0.5`), exigirá que la mira esté muy centrada.
3. **Integración con Permisos y Castigos:** Al igual que el Reach, notifica a los jugadores con permisos de staff (`bloon.alerts`) y ejecuta el comando de sanción automático al alcanzar el máximo de infracciones (VL)[cite: 3].

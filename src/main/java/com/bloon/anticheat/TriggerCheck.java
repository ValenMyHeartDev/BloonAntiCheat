package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.UUID;

public class TriggerCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> lastAttackTime = new HashMap<>();

    public TriggerCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("punishments.triggerbot.enabled", true) && 
            !config.getBoolean("checks.TriggerBot.enabled", true)) {
            return;
        }

        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null) return;

        long currentTime = System.currentTimeMillis();
        long lastTime = lastAttackTime.getOrDefault(player.getUniqueId(), 0L);
        long diff = currentTime - lastTime;
        lastAttackTime.put(player.getUniqueId(), currentTime);

        double currentBuffer = data.getTriggerBuffer();

        // 1. Reducción natural de búfer si el intervalo entre golpes es humano/normal (> 150 ms)
        if (diff > 150) {
            currentBuffer = Math.max(0.0, currentBuffer - 1.5);
            data.setTriggerBuffer(currentBuffer);
            return; // Golpe legítimo: se permite infractor 0% interrupción
        }

        // 2. Si los golpes son inhumanamente instantáneos (< 70 ms repetidos continuamente), aumentamos el búfer
        if (diff < 70) {
            currentBuffer += 1.2;
        } else {
            currentBuffer += 0.4;
        }

        data.setTriggerBuffer(currentBuffer);

        double threshold = config.getDouble("punishments.triggerbot.buffer-threshold", 8.0);

        // 3. Solo cuando el ritmo es consistentemente sospechoso y supera el umbral, se anula el golpe
        if (currentBuffer >= threshold) {
            event.setCancelled(true);

            AlertsUtil.sendAlert(player, "TriggerBot", "Ataque no legítimo bloqueado (Buffer: " + String.format("%.1f", currentBuffer) + ")");

            // Disminuimos ligeramente el búfer en vez de borrarlo para seguir bloqueando si mantiene el hack
            data.setTriggerBuffer(currentBuffer - 2.0);
        }
    }
}

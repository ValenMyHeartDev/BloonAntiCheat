package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class FlyCheck implements Listener {

    private final Main plugin;
    // Control de ticks en el aire para evitar falsos positivos al saltar
    private final HashMap<UUID, Integer> airTicks = new HashMap<>();

    public FlyCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("bloon.bypass") || player.getAllowFlight() || player.isInsideVehicle() || player.isGliding()) {
            airTicks.remove(player.getUniqueId());
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("checks.movement.fly.enabled", true)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double deltaY = to.getY() - from.getY();
        UUID uuid = player.getUniqueId();

        // Si el jugador está tocando el suelo, reseteamos su contador de aire
        if (player.isOnGround() || from.getBlock().isLiquid() || to.getBlock().isLiquid()) {
            airTicks.put(uuid, 0);
            return;
        }

        // Si está en el aire, aumentamos el contador de ticks suspendido
        int ticks = airTicks.getOrDefault(uuid, 0) + 1;
        airTicks.put(uuid, ticks);

        // Un salto legítimo dura pocos ticks subiendo y luego cae. 
        // Si lleva más de 4 ticks seguidos en el aire sin tocar el suelo y sin caer (flotando o subiendo con fly), se detecta.
        if (ticks > 4 && deltaY >= -0.08) {
            event.setCancelled(true);
            player.teleport(from);

            String message = "[BloonAC] " + player.getName() + " falló Fly / Levitación (Ticks en aire: " + ticks + ")";
            Bukkit.getLogger().info(message);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("bloon.alerts")) {
                    staff.sendMessage("§c" + message);
                }
            }
        }
    }
}

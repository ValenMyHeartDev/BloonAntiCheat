package com.bloon.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;

public class MovementAndExploitCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> lastMovePacket = new HashMap<>();

    public MovementAndExploitCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass") || player.isInsideVehicle() || player.isGliding()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        double toY = event.getTo().getY();
        double fromY = event.getFrom().getY();
        double deltaY = toY - fromY;

        // 1. Detección de Fly / PacketFly (Elevación anómala sostenida sin volar ni saltar legítimamente)
        if (config.getBoolean("checks.movement.fly.enabled", true)) {
            if (deltaY > 0.42 && !player.isOnGround() && player.getFallDistance() == 0) {
                AlertsUtil.AlertType alertType = (deltaY > 1.2) ? AlertsUtil.AlertType.V : AlertsUtil.AlertType.F;
                
                AlertsUtil.sendAlert(
                    player,
                    "Fly / PacketFly",
                    "Elevación ilegal (DeltaY: " + String.format("%.2f", deltaY) + ")",
                    alertType
                );
            }
        }

        // 2. Detección de Speed / Timer / LongJump (Velocidad horizontal excesiva)
        if (config.getBoolean("checks.movement.speed", true)) {
            double distanceSquared = event.getFrom().toVector().setY(0).distanceSquared(event.getTo().toVector().setY(0));
            // Límite aproximado de velocidad al correr/sprint sin pociones
            if (distanceSquared > 0.85 && !player.isFlying()) {
                AlertsUtil.sendAlert(
                    player,
                    "Speed / LongJump",
                    "Distancia horizontal anómala (" + String.format("%.2f", Math.sqrt(distanceSquared)) + " bloc/s)",
                    AlertsUtil.AlertType.P
                );
            }
        }

        // 3. Detección de BadPackets / PingSpoof / Timer (Desfase temporal de paquetes de movimiento)
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastMovePacket.getOrDefault(uuid, now);
        long diff = now - last;
        lastMovePacket.put(uuid, now);

        // Si el cliente inunda o detiene los paquetes de movimiento de forma antinatural
        if (config.getBoolean("checks.exploits.badpackets", true)) {
            if (diff > 1000 && !player.isDead()) { // Más de 1 segundo sin reportar posición estando activo
                AlertsUtil.sendAlert(
                    player,
                    "BadPackets / Freeze",
                    "Ausencia prolongada de paquetes (" + diff + "ms)",
                    AlertsUtil.AlertType.F
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        // Reseteamos el control temporal al teletransportarse para evitar falsos positivos
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN || 
            event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            lastMovePacket.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }
}

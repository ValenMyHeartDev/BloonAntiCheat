package com.bloon.anticheat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

public class PvPCombatCheck implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Long> lastPvpInteraction = new HashMap<>();

    public PvPCombatCheck(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("bloon.bypass")) return;

        FileConfiguration config = plugin.getConfig();
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) return;

        if (action == Action.RIGHT_CLICK_BLOCK) {
            boolean isAnchor = clickedBlock.getType() == Material.RESPAWN_ANCHOR;
            boolean isBed = clickedBlock.getType().name().contains("_BED");

            if ((isAnchor && config.getBoolean("checks.combat.anchoraura", true)) || 
                (isBed && config.getBoolean("checks.combat.bedaura", true))) {
                
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                long last = lastPvpInteraction.getOrDefault(uuid, 0L);
                long diff = now - last;
                lastPvpInteraction.put(uuid, now);

                // Sensibilidad al máximo: Menos de 120ms bloquea y alerta
                if (diff > 0 && diff < 120) {
                    event.setCancelled(true);
                    
                    String hackName = isAnchor ? "AnchorAura" : "BedAura";
                    AlertsUtil.sendAlert(
                        player, 
                        hackName, 
                        "Uso automatizado instantáneo (" + diff + "ms)", 
                        AlertsUtil.AlertType.V
                    );
                }
            }
        }
    }
}

package com.bloon.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Si el jugador está en la lista de muteados, bloqueamos el mensaje
        if (SanctionsManager.isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            SanctionsManager.SanctionData data = SanctionsManager.getMutedPlayers().get(player.getUniqueId());
            
            player.sendMessage("§c[BloonAC] No puedes hablar en el chat porque estás MUTEADO.");
            if (data != null) {
                player.sendMessage("§7Razón: §e" + data.getReason() + " §8| §7Duración: §e" + data.getDuration());
            }
        }
    }
}

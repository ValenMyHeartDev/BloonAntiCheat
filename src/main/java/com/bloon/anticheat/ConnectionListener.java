package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetSocketAddress;

public class ConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) return;

        String ip = address.getAddress().getHostAddress();

        // Verificar si la IP actual está registrada en el mapa de IPs baneadas de SanctionsManager
        if (SanctionsManager.getBannedIPs().containsKey(ip)) {
            SanctionsManager.SanctionData originalData = SanctionsManager.getBannedIPs().get(ip);
            
            // Notificar al staff en tiempo real
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("bloon.alerts")) {
                    staff.sendMessage("§c[BloonAC] ¡Alerta de Multicuenta! §e" + player.getName() + " §7intentó entrar con la IP baneada de §c" + originalData.getPlayerName());
                }
            }
        }
    }
}

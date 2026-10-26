package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AlertsUtil {

    private static final String ALERT_PERMISSION = "bloon.alerts";

    /**
     * Envía una alerta formateada a la consola y a todos los jugadores con permiso de staff.
     */
    public static void sendAlert(Player player, String checkName, String details) {
        // Formato visual de la alerta con colores profesionales
        String message = ChatColor.translateAlternateColorCodes('&', 
            "&8[&cBloonAC&8] &e" + player.getName() + " &7falló en &c" + checkName + " &7(&f" + details + "&7)");

        // 1. Enviar a la consola del servidor para registros (logs)
        Bukkit.getConsoleSender().sendMessage(message);

        // 2. Enviar a todos los jugadores en línea que tengan el permiso de staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission(ALERT_PERMISSION)) {
                staff.sendMessage(message);
            }
        }
    }
}

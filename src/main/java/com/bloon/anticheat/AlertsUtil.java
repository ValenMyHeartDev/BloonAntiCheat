package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AlertsUtil {

    public enum AlertType {
        F("&e[F - No Confirmable]"),
        P("&6[P - Posiblemente]"),
        V("&c[V - Cheat Confirmado]");

        private final String prefix;

        AlertType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public static void sendAlert(Player player, String checkName, String details, AlertType type) {
        String message = ChatColor.translateAlternateColorCodes('&', 
            "&8[&cBloonAC&8] " + type.getPrefix() + " &f" + player.getName() + " &7falló &c" + checkName + " &7(" + details + ")"
        );

        // Enviar a la consola del servidor
        Bukkit.getConsoleSender().sendMessage(message);

        // Enviar a los miembros del staff con el permiso correspondiente
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("bloon.alerts")) {
                staff.sendMessage(message);
            }
        }
    }
}

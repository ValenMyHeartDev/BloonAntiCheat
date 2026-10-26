package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AlertsUtil {

    public enum AlertType {
        F, // Flag (Sospecha / Bajo)
        P, // Punishment / Probable
        V  // Violation / Inmediato
    }

    public static void sendAlert(Player player, String checkName, String details, AlertType type) {
        String prefix = "[BloonAC] ";
        String message = prefix + "§c" + player.getName() + " §7falló §e" + checkName + " §8[" + type.name() + "] §7- " + details;
        
        // Enviar a consola
        Bukkit.getLogger().info(message);

        // Enviar a administradores con el permiso
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("bloon.alerts")) {
                staff.sendMessage(message);
            }
        }
    }
}

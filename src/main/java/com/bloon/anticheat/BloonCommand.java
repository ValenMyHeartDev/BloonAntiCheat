package com.bloon.anticheat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class BloonCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public BloonCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bloon.admin")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para ejecutar este comando.");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "[BloonAC] Configuración recargada correctamente.");
            return true;
        }

        // Menú de ayuda con la leyenda de los niveles de alerta (F, P, V)
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lBloon AntiCheat V1 &7- Ayuda & Niveles"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/bloon &7- Muestra este menú de ayuda"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/bloon reload &7- Recarga la configuración"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lNiveles de Alerta:"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e[F] &7F - No Confirmable (Ligeros desfases / Ping)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &6[P] &7P - Posiblemente (Patrón sospechoso continuo)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &c[V] &7V - Cheat Confirmado (Uso flagrante de hacks)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("reload");
        }
        return completions;
    }
}

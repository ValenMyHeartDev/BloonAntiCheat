package com.bloon.anticheat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class BloonCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;

    public BloonCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bloon.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /bloon <sanciones|reload>");
            return true;
        }

        if (args[0].equalsIgnoreCase("sanciones")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Este subcomando solo puede usarse dentro del juego.");
                return true;
            }
            new PunishmentMenu(plugin).open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configuración de Bloon AntiCheat recargada.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Uso: /bloon <sanciones|reload>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            for (String option : List.of("sanciones", "reload")) {
                if (option.startsWith(args[0].toLowerCase())) {
                    options.add(option);
                }
            }
        }
        return options;
    }
}

package com.bloon.anticheat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BloonCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public BloonCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bloon.admin")) {
            sender.sendMessage("§cNo tienes permisos para usar este comando.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8[§bBloonAC§8] §7Comandos disponibles:");
            sender.sendMessage("§e/bloon reload §7- Recarga la configuración.");
            sender.sendMessage("§e/bloon menu §7- Panel general del anticheat.");
            sender.sendMessage("§e/bloon sanciones §7- Panel interactivo de baneados y muteados.");
            sender.sendMessage("§e/bloon banear §7- Abre el menú para sancionar jugadores.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§a[BloonAC] ¡Configuración recargada exitosamente!");
            return true;
        }

        if (args[0].equalsIgnoreCase("sanciones")) {
            if (!(sender instanceof Player)) return true;
            BloonGUIListener.openMainSanctionsMenu((Player) sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("banear")) {
            if (!(sender instanceof Player)) return true;
            BloonGUIListener.openPlayerListMenu((Player) sender);
            return true;
        }

        sender.sendMessage("§cComando desconocido. Usa §e/bloon help§c.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("reload");
            completions.add("menu");
            completions.add("sanciones");
            completions.add("banear");
        }
        return completions;
    }
}

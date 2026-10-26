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
            sender.sendMessage("§e/bloon reload §7- Recarga la configuración del plugin.");
            sender.sendMessage("§e/bloon menu §7- Muestra el estado y menú del anticheat.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§a[BloonAC] ¡Configuración recargada exitosamente!");
            return true;
        }

        if (args[0].equalsIgnoreCase("menu")) {
            sender.sendMessage("§8================ §bBloon AntiCheat §8================");
            sender.sendMessage("§7Estado del servidor: §aProtegido");
            sender.sendMessage("§7Módulo Fly: " + (plugin.getConfig().getBoolean("checks.movement.fly.enabled") ? "§aActivado" : "§cDesactivado"));
            sender.sendMessage("§8=================================================");
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
        }
        return completions;
    }
}

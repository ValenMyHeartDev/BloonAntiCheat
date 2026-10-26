package com.bloon.anticheat;

import org.bukkit.Bukkit;
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
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar los comandos de Bloon AntiCheat.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "sanciones":
            case "menu":
            case "gui":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Este subcomando solo puede usarse dentro del juego.");
                    return true;
                }
                new PunishmentMenu(plugin).open(player);
                return true;

            case "reload":
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    "&8[&cBloonAC&8] &a¡Configuración de Bloon AntiCheat recargada correctamente!"));
                return true;

            case "alerts":
            case "alertas":
                if (sender instanceof Player player) {
                    boolean hasAlerts = player.hasPermission("bloon.alerts");
                    if (hasAlerts) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            "&8[&cBloonAC&8] &eNotificaciones de Staff: &aACTIVADAS &7(Permiso: bloon.alerts)"));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            "&8[&cBloonAC&8] &cNecesitas el permiso &ebloon.alerts &cpara recibir avisos."));
                    }
                } else {
                    sender.sendMessage("Solo los jugadores en el juego pueden verificar las alertas de chat.");
                }
                return true;

            case "stats":
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso correcto: /bloon stats <jugador>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "El jugador no se encuentra en línea.");
                    return true;
                }
                PlayerData data = plugin.getPlayerData(target.getUniqueId());
                sender.sendMessage(ChatColor.DARK_GRAY + "----------------------------------------");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lEstadísticas de " + target.getName()));
                sender.sendMessage(ChatColor.GRAY + "■ TriggerBot Buffer: " + ChatColor.YELLOW + String.format("%.2f", data.getTriggerBuffer()));
                sender.sendMessage(ChatColor.GRAY + "■ Latencia (Ping): " + ChatColor.GREEN + target.getPing() + "ms");
                sender.sendMessage(ChatColor.DARK_GRAY + "----------------------------------------");
                return true;

            default:
                sendHelpMenu(sender);
                return true;
        }
    }

    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + "----------------------------------------");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lBloon AntiCheat V1 &7- Comandos Staff"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/bloon help &7- Muestra esta guía e introducción a los comandos."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/bloon menu &7- Abre el menú visual (GUI) con el historial de sanciones."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/bloon reload &7- Recarga los parámetros del config.yml en tiempo real."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/bloon alerts &7- Comprueba el estado de las alertas de trampa en chat."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/bloon stats <jugador> &7- Muestra el buffer de trampa y ping en vivo."));
        sender.sendMessage(ChatColor.DARK_GRAY + "----------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        
        if (args.length == 1) {
            for (String option : List.of("help", "menu", "sanciones", "reload", "alerts", "stats")) {
                if (option.startsWith(args[0].toLowerCase())) {
                    options.add(option);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("info"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    options.add(player.getName());
                }
            }
        }
        
        return options;
    }
}

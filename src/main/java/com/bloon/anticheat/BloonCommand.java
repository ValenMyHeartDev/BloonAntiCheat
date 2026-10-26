package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            sender.sendMessage("§e/bloon sanciones §7- Panel de baneados y muteados.");
            sender.sendMessage("§e/bloon banear §7- Abre el menú para sancionar jugadores.");
            sender.sendMessage("§e/bloon ignore <jugador> §7- Ignora o audita a un jugador para pruebas.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§a[BloonAC] ¡Configuración recargada exitosamente!");
            return true;
        }

        if (args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cEste comando solo puede ser usado por un jugador.");
                return true;
            }
            openGeneralMenu((Player) sender);
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

        if (args[0].equalsIgnoreCase("ignore")) {
            if (args.length < 2) {
                sender.sendMessage("§cUso correcto: /bloon ignore <jugador>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§c[BloonAC] El jugador no está conectado o no existe.");
                return true;
            }

            boolean isIgnored = CheckManager.toggleIgnore(target.getUniqueId());
            if (isIgnored) {
                sender.sendMessage("§a[BloonAC] Ahora el anticheat IGNORA por completo a §e" + target.getName() + " §a(puede probar hacks sin logs).");
                target.sendMessage("§e[BloonAC] El staff te ha puesto en modo de prueba (Ignorado por el Anticheat).");
            } else {
                sender.sendMessage("§c[BloonAC] El anticheat vuelve a DETECTAR a §e" + target.getName() + ".");
                target.sendMessage("§c[BloonAC] Ya no estás ignorado por el Anticheat.");
            }
            return true;
        }

        sender.sendMessage("§cComando desconocido. Usa §e/bloon help§c.");
        return true;
    }

    private void openGeneralMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Panel General");

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bEstado del Anticheat");
            List<String> lore = new ArrayList<>();
            lore.add("§7Módulo Fly: §aActivo");
            lore.add("§7Módulo ClickTP: §aActivo");
            lore.add("§7Módulo Criticals: §aActivo");
            meta.setLore(lore);
            info.setItemMeta(meta);
        }
        inv.setItem(13, info);

        player.openInventory(inv);
        player.sendMessage("§a[BloonAC] Abriendo panel general...");
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
            completions.add("ignore");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("ignore")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        }
        return completions;
    }
}

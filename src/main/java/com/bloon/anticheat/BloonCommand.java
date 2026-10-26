package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.Map;
import java.util.UUID;

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
            sender.sendMessage("§e/bloon sanciones §7- Lista interactiva de jugadores baneados.");
            sender.sendMessage("§e/bloon banear §7- Abre el menú para banear/mutear usuarios.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§a[BloonAC] ¡Configuración recargada exitosamente!");
            return true;
        }

        if (args[0].equalsIgnoreCase("sanciones")) {
            if (!(sender instanceof Player)) return true;
            openBannedPlayersMenu((Player) sender);
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

    private void openBannedPlayersMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] Sanciones Activas");
        Map<UUID, SanctionsManager.SanctionData> bans = SanctionsManager.getBannedPlayers();

        for (SanctionsManager.SanctionData data : bans.values()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = skull.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§c" + data.getPlayerName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Baneado T: §e" + data.getDuration());
                lore.add("§7Razón: §f" + data.getReason());
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            inv.addItem(skull);
        }

        player.openInventory(inv);
        player.sendMessage("§a[BloonAC] Abriendo lista de jugadores sancionados...");
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

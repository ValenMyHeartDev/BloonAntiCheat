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
            sender.sendMessage("§e/bloon menu §7- Abre el panel gráfico de control y sanciones.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§a[BloonAC] ¡Configuración recargada exitosamente!");
            return true;
        }

        if (args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cEste comando solo puede ser ejecutado por un jugador dentro del servidor.");
                return true;
            }

            Player player = (Player) sender;
            openSanctionsMenu(player);
            return true;
        }

        sender.sendMessage("§cComando desconocido. Usa §e/bloon help§c.");
        return true;
    }

    private void openSanctionsMenu(Player player) {
        // Crear un inventario GUI de 27 espacios (3 filas) para el panel de sanciones
        Inventory menu = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Panel de Sanciones");

        // Ítem 1: Estado General
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§bEstado de BloonAC");
            List<String> lore = new ArrayList<>();
            lore.add("§7Protección contra Fly: " + (plugin.getConfig().getBoolean("checks.movement.fly.enabled", true) ? "§aActivo" : "§cInactivo"));
            lore.add("§7Protección contra ClickTP: " + (plugin.getConfig().getBoolean("checks.movement.clicktp.enabled", true) ? "§aActivo" : "§cInactivo"));
            lore.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        menu.setItem(4, info);

        // Ítem 2: Acción rápida de Banear último tramposo o alertas
        ItemStack alertToggle = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta alertMeta = alertToggle.getItemMeta();
        if (alertMeta != null) {
            alertMeta.setDisplayName("§c§lAlertas de Staff");
            List<String> lore = new ArrayList<>();
            lore.add("§7Las alertas en tiempo real");
            lore.add("§7están activas para el staff.");
            alertMeta.setLore(lore);
            alertToggle.setItemMeta(alertMeta);
        }
        menu.setItem(11, alertToggle);

        // Ítem 3: Recargar config desde la GUI
        ItemStack reloadItem = new ItemStack(Material.EMERALD);
        ItemMeta reloadMeta = reloadItem.getItemMeta();
        if (reloadMeta != null) {
            reloadMeta.setDisplayName("§a§lRecargar Configuración");
            reloadItem.setItemMeta(reloadMeta);
        }
        menu.setItem(15, reloadItem);

        // Abrir el inventario al jugador
        player.openInventory(menu);
        player.sendMessage("§a[BloonAC] Abriendo el panel de control y sanciones...");
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

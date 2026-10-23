package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PunishmentMenu {

    private final JavaPlugin plugin;

    public PunishmentMenu(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Bloon AntiCheat " + ChatColor.DARK_RED + "» Sanciones");

        FileConfiguration config = plugin.getConfig();
        if (config.isConfigurationSection("sanctions")) {
            int slot = 0;
            for (String key : config.getConfigurationSection("sanctions").getKeys(false)) {
                if (slot >= 54) break;

                String playerName = config.getString("sanctions." + key + ".player");
                String type = config.getString("sanctions." + key + ".type");
                String reason = config.getString("sanctions." + key + ".reason");
                String duration = config.getString("sanctions." + key + ".duration");
                String date = config.getString("sanctions." + key + ".date");

                ItemStack skull = createPunishmentSkull(playerName, type, reason, duration, date);
                gui.setItem(slot, skull);
                slot++;
            }
        }

        admin.openInventory(gui);
    }

    private ItemStack createPunishmentSkull(String playerName, String type, String reason, String duration, String date) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            meta.setOwningPlayer(target);

            meta.setDisplayName(ChatColor.RED + ChatColor.BOLD.toString() + playerName);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + "------------------------");
            lore.add(ChatColor.GRAY + "■ Usuario: " + ChatColor.WHITE + playerName);
            lore.add(ChatColor.GRAY + "■ Tipo: " + ChatColor.YELLOW + type);
            lore.add(ChatColor.GRAY + "■ Razón: " + ChatColor.RED + reason);
            lore.add(ChatColor.GRAY + "■ Duración: " + ChatColor.GREEN + duration);
            lore.add(ChatColor.GRAY + "■ Fecha: " + ChatColor.AQUA + date);
            lore.add(ChatColor.DARK_GRAY + "------------------------");

            meta.setLore(lore);
            skull.setItemMeta(meta);
        }

        return skull;
    }
}

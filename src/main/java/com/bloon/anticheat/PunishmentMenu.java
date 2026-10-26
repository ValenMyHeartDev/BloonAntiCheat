package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PunishmentMenu {

    public static final String TITLE = ChatColor.DARK_GRAY + "Bloon AntiCheat " + ChatColor.DARK_RED + "» Sanciones";

    private final JavaPlugin plugin;

    public PunishmentMenu(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin) {
        openMenu(admin);
    }

    public void openMenu(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection sanctionsSection = config.getConfigurationSection("sanctions");

        if (sanctionsSection != null) {
            int slot = 0;
            for (String key : sanctionsSection.getKeys(false)) {
                if (slot >= 54) break;

                // Lee los campos configurados en tu config.yml
                String name = config.getString("sanctions." + key + ".name", "&cSanción");
                String materialName = config.getString("sanctions." + key + ".material", "PLAYER_HEAD");
                String ownerName = config.getString("sanctions." + key + ".owner", "Notch");
                List<String> rawLore = config.getStringList("sanctions." + key + ".lore");

                ItemStack item = createCustomItem(materialName, ownerName, name, rawLore);
                gui.setItem(slot, item);
                slot++;
            }
        }

        admin.openInventory(gui);
    }

    private ItemStack createCustomItem(String materialName, String ownerName, String displayName, List<String> rawLore) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.PLAYER_HEAD;
        }

        ItemStack item = new ItemStack(material);
        
        // Si es una cabeza de jugador, aplicamos su skin con el owner
        if (item.getItemMeta() instanceof SkullMeta skullMeta) {
            try {
                OfflinePlayer target = Bukkit.getOfflinePlayer(ownerName);
                skullMeta.setOwningPlayer(target);
            } catch (Exception ignored) {}
            item.setItemMeta(skullMeta);
        }

        if (item.hasItemMeta() || item.getType() == Material.PLAYER_HEAD) {
            var meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

                List<String> formattedLore = new ArrayList<>();
                for (String line : rawLore) {
                    formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(formattedLore);
                item.setItemMeta(meta);
            }
        }

        return item;
    }
}

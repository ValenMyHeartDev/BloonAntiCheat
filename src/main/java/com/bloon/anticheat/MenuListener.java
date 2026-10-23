package com.bloon.anticheat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class MenuListener implements Listener {
    private final Main plugin;

    public MenuListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isPunishmentMenu(event.getView().getTitle())) return;

        event.setCancelled(true);

        // Items are intentionally display-only. No punishment is executed by clicking.
        if (event.getWhoClicked() instanceof Player player
                && event.getClickedInventory() != null
                && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            player.sendMessage(ChatColor.GRAY + "Menú de sanciones: solo lectura.");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isPunishmentMenu(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }

    private boolean isPunishmentMenu(String title) {
        return PunishmentMenu.TITLE.equals(title);
    }
}

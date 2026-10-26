package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BloonGUIListener implements Listener {

    private final Main plugin;
    // Almacenamos temporalmente qué staff está sancionando a qué objetivo y el tipo (BAN/MUTE)
    private static final java.util.Map<UUID, TargetSelection> targetCache = new java.util.HashMap<>();

    private static class TargetSelection {
        UUID targetUUID;
        String targetName;
        String actionType; // "BAN" o "MUTE"
        String duration;
    }

    public BloonGUIListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player staff = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.startsWith("§8[§cBloonAC§8]")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // 1. Menú Principal de Sanciones (Lista de baneados)
            if (title.equals("§8[§cBloonAC§8] Sanciones Activas")) {
                return;
            }

            // 2. Menú para Seleccionar Jugador Online (/bloon banear)
            if (title.equals("§8[§cBloonAC§8] Seleccionar Jugador")) {
                if (clicked.getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        OfflinePlayer target = meta.getOwningPlayer();
                        TargetSelection sel = new TargetSelection();
                        sel.targetUUID = target.getUniqueId();
                        sel.targetName = target.getName();
                        targetCache.put(staff.getUniqueId(), sel);

                        openActionTypeMenu(staff, sel.targetName);
                    }
                }
                return;
            }

            // 3. Menú para Elegir Acción (Banear o Mutear)
            if (title.equals("§8[§cBloonAC§8] Elegir Sanción")) {
                TargetSelection sel = targetCache.get(staff.getUniqueId());
                if (sel == null) return;

                if (clicked.getType() == Material.RED_WOOL) {
                    sel.actionType = "BAN";
                    openDurationMenu(staff, "BAN");
                } else if (clicked.getType() == Material.YELLOW_WOOL) {
                    sel.actionType = "MUTE";
                    openDurationMenu(staff, "MUTE");
                }
                return;
            }

            // 4. Menú de Duración
            if (title.contains("Duración")) {
                TargetSelection sel = targetCache.get(staff.getUniqueId());
                if (sel == null) return;

                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    sel.duration = meta.getDisplayName();
                    openReasonMenu(staff);
                }
                return;
            }

            // 5. Menú de Razones
            if (title.equals("§8[§cBloonAC§8] Seleccionar Razón")) {
                TargetSelection sel = targetCache.get(staff.getUniqueId());
                if (sel == null) return;

                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    String reason = meta.getDisplayName();
                    if (sel.actionType.equals("BAN")) {
                        SanctionsManager.banPlayer(sel.targetUUID, sel.targetName, reason, sel.duration);
                        staff.sendMessage("§a[BloonAC] ¡Jugador " + sel.targetName + " baneado exitosamente!");
                    } else {
                        staff.sendMessage("§a[BloonAC] ¡Jugador " + sel.targetName + " silenciado por " + sel.duration + " (" + reason + ")!");
                    }
                    staff.closeInventory();
                    targetCache.remove(staff.getUniqueId());
                }
                return;
            }
        }
    }

    public static void openPlayerListMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] Seleccionar Jugador");
        for (Player online : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(online);
                meta.setDisplayName("§e" + online.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Clic para sancionar a este usuario.");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.addItem(head);
        }
        staff.openInventory(inv);
    }

    private void openActionTypeMenu(Player staff, String targetName) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Elegir Sanción");
        
        ItemStack ban = new ItemStack(Material.RED_WOOL);
        ItemMeta bm = ban.getItemMeta();
        bm.setDisplayName("§c§lBANEAR");
        List<String> loreB = new ArrayList<>();
        loreB.add("§7Sancionar a " + targetName + " fuera del servidor.");
        bm.setLore(loreB);
        ban.setItemMeta(bm);
        inv.setItem(11, ban);

        ItemStack mute = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta mm = mute.getItemMeta();
        mm.setDisplayName("§e§lMUTEAR");
        List<String> loreM = new ArrayList<>();
        loreM.add("§7Silenciar temporalmente a " + targetName + ".");
        mm.setLore(loreM);
        mute.setItemMeta(mm);
        inv.setItem(15, mute);

        staff.openInventory(inv);
    }

    private void openDurationMenu(Player staff, String type) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Seleccionar Duración");
        
        if (type.equals("MUTE")) {
            // Muteos de 1d a 30d y Permanente
            String[] durations = {"1 Día", "3 Días", "7 Días", "14 Días", "30 Días", "Permanente"};
            for (int i = 0; i < durations.length; i++) {
                ItemStack item = new ItemStack(Material.CLOCK);
                ItemMeta m = item.getItemMeta();
                m.setDisplayName(durations[i]);
                item.setItemMeta(m);
                inv.setItem(10 + i, item);
            }
        } else {
            // Baneos de 15d hasta 200d y Permanente
            String[] durations = {"15 Días", "30 Días", "60 Días", "90 Días", "200 Días", "Permanente"};
            for (int i = 0; i < durations.length; i++) {
                ItemStack item = new ItemStack(Material.ANVIL);
                ItemMeta m = item.getItemMeta();
                m.setDisplayName(durations[i]);
                item.setItemMeta(m);
                inv.setItem(10 + i, item);
            }
        }
        staff.openInventory(inv);
    }

    private void openReasonMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Seleccionar Razón");
        String[] reasons = {"Uso de Trampas (Cheating)", "Insultos al Staff", "Toxicidad / Mal Vocabulario", "Flood / Spam", "Uso de Exploits"};
        
        for (int i = 0; i < reasons.length; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta m = item.getItemMeta();
            m.setDisplayName(reasons[i]);
            item.setItemMeta(m);
            inv.setItem(10 + i, item);
        }
        staff.openInventory(inv);
    }
}

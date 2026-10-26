package com.bloon.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BloonGUIListener implements Listener {

    private final Main plugin;
    private static final Map<UUID, TargetSelection> targetCache = new HashMap<>();

    private static class TargetSelection {
        UUID targetUUID;
        String targetName;
        String actionType;
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

        if (title.contains("BloonAC")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // 1. Menú Principal de Sanciones (3 opciones: Bans, IPs, Mutes)
            if (title.contains("Gestión de Sanciones")) {
                if (clicked.getType() == Material.REDSTONE_BLOCK) {
                    openBannedListMenu(staff);
                } else if (clicked.getType() == Material.IRON_BARS) {
                    openBannedIPsMenu(staff);
                } else if (clicked.getType() == Material.YELLOW_WOOL) {
                    openMutedListMenu(staff);
                }
                return;
            }

            // 2. Submenú: Lista de Baneados (Clic para Desbanear Jugador)
            if (title.contains("Jugadores Baneados")) {
                if (clicked.getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        OfflinePlayer target = meta.getOwningPlayer();
                        SanctionsManager.unbanPlayer(target.getUniqueId());
                        staff.sendMessage("§a[BloonAC] ¡Has desbaneado a " + target.getName() + " exitosamente!");
                        staff.closeInventory();
                    }
                }
                return;
            }

            // 3. Submenú: Lista de IPs Baneadas (Clic para Desbanear IP)
            if (title.contains("IPs Baneadas")) {
                if (clicked.getType() == Material.PAPER) {
                    ItemMeta meta = clicked.getItemMeta();
                    if (meta != null && meta.getDisplayName() != null) {
                        String ip = meta.getDisplayName().replace("§cIP: ", "").trim();
                        SanctionsManager.unbanIP(ip);
                        staff.sendMessage("§a[BloonAC] ¡Has desbaneado la IP: §e" + ip + " §aexitosamente!");
                        staff.closeInventory();
                    }
                }
                return;
            }

            // 4. Submenú: Lista de Muteados (Clic para Desmutear)
            if (title.contains("Jugadores Muteados")) {
                if (clicked.getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        OfflinePlayer target = meta.getOwningPlayer();
                        SanctionsManager.unmutePlayer(target.getUniqueId());
                        staff.sendMessage("§a[BloonAC] ¡Has desmuteado a " + target.getName() + " exitosamente!");
                        staff.closeInventory();
                    }
                }
                return;
            }

            // 5. Menú: Seleccionar Jugador para sancionar
            if (title.contains("Seleccionar Jugador")) {
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

            // 6. Menú: Elegir Acción
            if (title.contains("Elegir Sanción")) {
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

            // 7. Menú: Seleccionar Duración
            if (title.contains("Seleccionar Duración")) {
                TargetSelection sel = targetCache.get(staff.getUniqueId());
                if (sel == null) return;

                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    sel.duration = meta.getDisplayName();
                    openReasonMenu(staff, sel.actionType);
                }
                return;
            }

            // 8. Menú: Seleccionar Razón
            if (title.contains("Seleccionar Razón")) {
                TargetSelection sel = targetCache.get(staff.getUniqueId());
                if (sel == null) return;

                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    String reason = meta.getDisplayName();
                    if ("BAN".equals(sel.actionType)) {
                        SanctionsManager.banPlayer(sel.targetUUID, sel.targetName, reason, sel.duration);
                        staff.sendMessage("§a[BloonAC] ¡Jugador " + sel.targetName + " baneado (Nombre e IP) por " + reason + "!");
                    } else {
                        SanctionsManager.mutePlayer(sel.targetUUID, sel.targetName, reason, sel.duration);
                        staff.sendMessage("§a[BloonAC] ¡Jugador " + sel.targetName + " muteado por " + reason + "!");
                    }
                    staff.closeInventory();
                    targetCache.remove(staff.getUniqueId());
                }
                return;
            }
        }
    }

    public static void openMainSanctionsMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Gestión de Sanciones");

        ItemStack bans = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta bm = bans.getItemMeta();
        if (bm != null) {
            bm.setDisplayName("§c§lJugadores Baneados");
            List<String> lore = new ArrayList<>();
            lore.add("§7Clic para ver jugadores y desbanear.");
            bm.setLore(lore);
            bans.setItemMeta(bm);
        }
        inv.setItem(10, bans);

        ItemStack ipBans = new ItemStack(Material.IRON_BARS);
        ItemMeta im = ipBans.getItemMeta();
        if (im != null) {
            im.setDisplayName("§c§lIPs Baneadas");
            List<String> lore = new ArrayList<>();
            lore.add("§7Clic para ver IPs bloqueadas y desbanearlas.");
            im.setLore(lore);
            ipBans.setItemMeta(im);
        }
        inv.setItem(13, ipBans);

        ItemStack mutes = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta mm = mutes.getItemMeta();
        if (mm != null) {
            mm.setDisplayName("§e§lJugadores Muteados");
            List<String> lore = new ArrayList<>();
            lore.add("§7Clic para ver muteados y desmutear.");
            mm.setLore(lore);
            mutes.setItemMeta(mm);
        }
        inv.setItem(16, mutes);

        staff.openInventory(inv);
    }

    public static void openBannedListMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] Jugadores Baneados");
        Map<UUID, SanctionsManager.SanctionData> bans = SanctionsManager.getBannedPlayers();

        for (SanctionsManager.SanctionData data : bans.values()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getUuid()));
                meta.setDisplayName("§c" + data.getPlayerName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Duración: §e" + data.getDuration());
                lore.add("§7Razón: §f" + data.getReason());
                lore.add("§7IP asociada: §8" + (data.getIp() != null ? data.getIp() : "Ninguna"));
                lore.add("");
                lore.add("§a¡Haz clic para DESBANEAR!");
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            inv.addItem(skull);
        }
        staff.openInventory(inv);
    }

    public static void openBannedIPsMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] IPs Baneadas");
        Map<String, SanctionsManager.SanctionData> ipBans = SanctionsManager.getBannedIPs();

        for (SanctionsManager.SanctionData data : ipBans.values()) {
            if (data.getIp() == null) continue;
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§cIP: " + data.getIp());
                List<String> lore = new ArrayList<>();
                lore.add("§7Usuario original: §e" + data.getPlayerName());
                lore.add("§7Razón: §f" + data.getReason());
                lore.add("§7Duración: §e" + data.getDuration());
                lore.add("");
                lore.add("§a¡Haz clic para DESBANEAR ESTA IP!");
                meta.setLore(lore);
                paper.setItemMeta(meta);
            }
            inv.addItem(paper);
        }
        staff.openInventory(inv);
    }

    public static void openMutedListMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] Jugadores Muteados");
        Map<UUID, SanctionsManager.SanctionData> mutes = SanctionsManager.getMutedPlayers();

        for (SanctionsManager.SanctionData data : mutes.values()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getUuid()));
                meta.setDisplayName("§e" + data.getPlayerName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Duración: §e" + data.getDuration());
                lore.add("§7Razón: §f" + data.getReason());
                lore.add("");
                lore.add("§a¡Haz clic para DESMUTEAR!");
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            inv.addItem(skull);
        }
        staff.openInventory(inv);
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
        if (bm != null) {
            bm.setDisplayName("§c§lBANEAR");
            List<String> loreB = new ArrayList<>();
            loreB.add("§7Sanción grave para " + targetName);
            bm.setLore(loreB);
            ban.setItemMeta(bm);
        }
        inv.setItem(11, ban);

        ItemStack mute = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta mm = mute.getItemMeta();
        if (mm != null) {
            mm.setDisplayName("§e§lMUTEAR");
            List<String> loreM = new ArrayList<>();
            loreM.add("§7Silenciar chat a " + targetName);
            mm.setLore(loreM);
            mute.setItemMeta(mm);
        }
        inv.setItem(15, mute);

        staff.openInventory(inv);
    }

    private void openDurationMenu(Player staff, String type) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Seleccionar Duración");
        
        if ("MUTE".equals(type)) {
            String[] durations = {"1 Hora", "6 Horas", "1 Día", "3 Días", "7 Días", "30 Días", "Permanente"};
            for (int i = 0; i < durations.length; i++) {
                ItemStack item = new ItemStack(Material.CLOCK);
                ItemMeta m = item.getItemMeta();
                if (m != null) {
                    m.setDisplayName(durations[i]);
                    item.setItemMeta(m);
                }
                inv.setItem(10 + i, item);
            }
        } else {
            String[] durations = {"15 Días", "30 Días", "60 Días", "90 Días", "150 Días", "200 Días", "Permanente"};
            for (int i = 0; i < durations.length; i++) {
                ItemStack item = new ItemStack(Material.ANVIL);
                ItemMeta m = item.getItemMeta();
                if (m != null) {
                    m.setDisplayName(durations[i]);
                    item.setItemMeta(m);
                }
                inv.setItem(10 + i, item);
            }
        }
        staff.openInventory(inv);
    }

    private void openReasonMenu(Player staff, String type) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Seleccionar Razón");
        
        String[] reasons;
        if ("BAN".equals(type)) {
            reasons = new String[] {
                "Uso de Hacks (Fly / Killaura / Reach)", 
                "Explotación de Bugs / Dupes", 
                "Uso de multicuentas / Ban Evading", 
                "Griefing masivo de estructuras", 
                "Amenazas de DoX / DDoST"
            };
        } else {
            reasons = new String[] {
                "Insultos graves / Falta de respeto", 
                "Toxicidad excesiva en el chat", 
                "Spam / Flood continuo", 
                "Promoción de otros servidores", 
                "Uso excesivo de mayúsculas / Provocación"
            };
        }

        for (int i = 0; i < reasons.length; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta m = item.getItemMeta();
            if (m != null) {
                m.setDisplayName(reasons[i]);
                item.setItemMeta(m);
            }
            inv.setItem(10 + i, item);
        }
        staff.openInventory(inv);
    }
}

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

            // 1. Menú Principal de Sanciones
            if (title.contains("Gestión de Sanciones")) {
                if (clicked.getType() == Material.REDSTONE_BLOCK) {
                    openBannedListMenu(staff);
                } else if (clicked.getType() == Material.IRON_BARS) {
                    openBannedIPsMenu(staff);
                } else if (clicked.getType() == Material.YELLOW_WOOL) {
                    openMutedListMenu(staff);
                } else if (clicked.getType() == Material.ENDER_EYE) {
                    openGlobalSanctionsMenu(staff);
                }
                return;
            }

            // 2. Menú GlobalS: Clic en cabeza para ver historial individual
            if (title.contains("Historial Global")) {
                if (clicked.getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        OfflinePlayer target = meta.getOwningPlayer();
                        openPlayerHistoryMenu(staff, target.getUniqueId(), target.getName());
                    }
                }
                return;
            }

            // 3. Submenús de listas normales (Baneados, IPs, Muteados)
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

            // 4. Selección de jugadores para sancionar
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

            if (title.contains("Seleccionar Razón")) {
                TargetSelection sel = targetCache.get(staff.getUniqueId());
                if (sel == null) return;

                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    String reason = meta.getDisplayName();
                    if ("BAN".equals(sel.actionType)) {
                        SanctionsManager.banPlayer(sel.targetUUID, sel.targetName, reason, sel.duration);
                        staff.sendMessage("§a[BloonAC] ¡Jugador " + sel.targetName + " baneado por " + reason + "!");
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
            bm.setLore(List.of("§7Clic para ver y desbanear."));
            bans.setItemMeta(bm);
        }
        inv.setItem(10, bans);

        ItemStack ipBans = new ItemStack(Material.IRON_BARS);
        ItemMeta im = ipBans.getItemMeta();
        if (im != null) {
            im.setDisplayName("§c§lIPs Baneadas");
            im.setLore(List.of("§7Clic para ver IPs bloqueadas."));
            ipBans.setItemMeta(im);
        }
        inv.setItem(12, ipBans);

        ItemStack mutes = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta mm = mutes.getItemMeta();
        if (mm != null) {
            mm.setDisplayName("§e§lJugadores Muteados");
            mm.setLore(List.of("§7Clic para ver muteados."));
            mutes.setItemMeta(mm);
        }
        inv.setItem(14, mutes);

        ItemStack globals = new ItemStack(Material.ENDER_EYE);
        ItemMeta gm = globals.getItemMeta();
        if (gm != null) {
            gm.setDisplayName("§b§lHistorial Global (GlobalS)");
            gm.setLore(List.of("§7Expedientes de todos los sancionados."));
            globals.setItemMeta(gm);
        }
        inv.setItem(16, globals);

        staff.openInventory(inv);
    }

    public static void openGlobalSanctionsMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] Historial Global");
        for (UUID uuid : SanctionsManager.getAllSanctionedPlayers()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(op);
                meta.setDisplayName("§e" + (op.getName() != null ? op.getName() : "Desconocido"));
                List<String> lore = new ArrayList<>();
                lore.add("§7Total de sanciones: §f" + SanctionsManager.getHistory(uuid).size());
                lore.add("");
                lore.add("§a¡Haz clic para ver su expediente!");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.addItem(head);
        }
        staff.openInventory(inv);
    }

    public static void openPlayerHistoryMenu(Player staff, UUID targetUUID, String targetName) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Expediente: " + targetName);
        List<SanctionsManager.SanctionRecord> records = SanctionsManager.getHistory(targetUUID);

        for (SanctionsManager.SanctionRecord record : records) {
            ItemStack item = new ItemStack("BAN".equals(record.getType()) ? Material.RED_CONCRETE : Material.YELLOW_CONCRETE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(("BAN".equals(record.getType()) ? "§c[BAN]" : "§e[MUTE]"));
                List<String> lore = new ArrayList<>();
                lore.add("§7Razón: §f" + record.getReason());
                lore.add("§7Duración: §e" + record.getDuration());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.addItem(item);
        }
        staff.openInventory(inv);
    }

    public static void openBannedListMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] Jugadores Baneados");
        for (SanctionsManager.SanctionData data : SanctionsManager.getBannedPlayers().values()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getUuid()));
                meta.setDisplayName("§c" + data.getPlayerName());
                meta.setLore(List.of("§7Duración: §e" + data.getDuration(), "§7Razón: §f" + data.getReason(), "", "§a¡Haz clic para DESBANEAR!"));
                skull.setItemMeta(meta);
            }
            inv.addItem(skull);
        }
        staff.openInventory(inv);
    }

    public static void openBannedIPsMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] IPs Baneadas");
        for (SanctionsManager.SanctionData data : SanctionsManager.getBannedIPs().values()) {
            if (data.getIp() == null) continue;
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§cIP: " + data.getIp());
                meta.setLore(List.of("§7Usuario: §e" + data.getPlayerName(), "§7Razón: §f" + data.getReason(), "", "§a¡Haz clic para DESBANEAR IP!"));
                paper.setItemMeta(meta);
            }
            inv.addItem(paper);
        }
        staff.openInventory(inv);
    }

    public static void openMutedListMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBloonAC§8] Jugadores Muteados");
        for (SanctionsManager.SanctionData data : SanctionsManager.getMutedPlayers().values()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getUuid()));
                meta.setDisplayName("§e" + data.getPlayerName());
                meta.setLore(List.of("§7Duración: §e" + data.getDuration(), "§7Razón: §f" + data.getReason(), "", "§a¡Haz clic para DESMUTEAR!"));
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
                meta.setLore(List.of("§7Clic para sancionar."));
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
        if (bm != null) { bm.setDisplayName("§c§lBANEAR"); ban.setItemMeta(bm); }
        inv.setItem(11, ban);

        ItemStack mute = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta mm = mute.getItemMeta();
        if (mm != null) { mm.setDisplayName("§e§lMUTEAR"); mute.setItemMeta(mm); }
        inv.setItem(15, mute);
        staff.openInventory(inv);
    }

    private void openDurationMenu(Player staff, String type) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Seleccionar Duración");
        String[] durations = "MUTE".equals(type) ? 
            new String[]{"1 Hora", "6 Horas", "1 Día", "3 Días", "7 Días", "30 Días", "Permanente"} :
            new String[]{"15 Días", "30 Días", "60 Días", "90 Días", "150 Días", "200 Días", "Permanente"};
        
        for (int i = 0; i < durations.length; i++) {
            ItemStack item = new ItemStack("MUTE".equals(type) ? Material.CLOCK : Material.ANVIL);
            ItemMeta m = item.getItemMeta();
            if (m != null) { m.setDisplayName(durations[i]); item.setItemMeta(m); }
            inv.setItem(10 + i, item);
        }
        staff.openInventory(inv);
    }

    private void openReasonMenu(Player staff, String type) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBloonAC§8] Seleccionar Razón");
        String[] reasons = "BAN".equals(type) ?
            new String[]{"Uso de Hacks", "Explotación de Bugs", "Ban Evading", "Griefing Masivo", "Amenazas"} :
            new String[]{"Insultos Graves", "Toxicidad", "Spam / Flood", "Promoción", "Provocación"};

        for (int i = 0; i < reasons.length; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta m = item.getItemMeta();
            if (m != null) { m.setDisplayName(reasons[i]); item.setItemMeta(m); }
            inv.setItem(10 + i, item);
        }
        staff.openInventory(inv);
    }
}

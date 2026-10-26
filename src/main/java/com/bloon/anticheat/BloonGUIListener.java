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

    private final Main plugin; // <--- Referencia al plugin
    private static final Map<UUID, TargetSelection> targetCache = new HashMap<>();

    // Constructor que recibe Main para evitar el error de símbolo
    public BloonGUIListener(Main plugin) {
        this.plugin = plugin;
    }

    // El resto de tus métodos de inventario y clics van aquí abajo...

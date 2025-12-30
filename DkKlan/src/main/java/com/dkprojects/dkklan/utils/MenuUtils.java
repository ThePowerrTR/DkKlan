package com.dkprojects.dkklan.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuUtils {

    public static ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, Arrays.asList(lore));
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> coloredLore = new ArrayList<>();
            if (lore != null) {
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createItem(Material material, String name, boolean glow, String... lore) {
        ItemStack item = createItem(material, name, lore);
        if (glow) {
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void fillBorders(Inventory inv, Material borderMaterial) {
        int size = inv.getSize();
        int rows = size / 9;
        
        ItemStack border = createItem(borderMaterial, "&8");
        
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border); // Top row
            inv.setItem(size - 9 + i, border); // Bottom row
        }
        
        for (int i = 0; i < rows; i++) {
            inv.setItem(i * 9, border); // Left column
            inv.setItem(i * 9 + 8, border); // Right column
        }
    }

    public static void fillBackground(Inventory inv, Material fillerMaterial) {
        ItemStack filler = createItem(fillerMaterial, "&8");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, filler);
            }
        }
    }
    
    public static String centerTitle(String title) {
        // Simple centering logic or just return formatted
        return ChatColor.translateAlternateColorCodes('&', title);
    }
}

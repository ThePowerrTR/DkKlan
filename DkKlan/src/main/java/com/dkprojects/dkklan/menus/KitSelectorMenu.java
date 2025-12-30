package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class KitSelectorMenu {

    private final DkKlan plugin;

    public KitSelectorMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Kit Seçimi");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.YELLOW_STAINED_GLASS_PANE);

        List<String> kits = plugin.getKitManager().getKitNames();
        
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (String kitName : kits) {
            if (index >= slots.length) break;

            List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.addAll(plugin.getKitManager().getKitPreview(kitName));
            lore.add("");
            lore.add("&eSeçmek için tıkla!");

            inv.setItem(slots[index++], com.dkprojects.dkklan.utils.MenuUtils.createItem(
                Material.IRON_CHESTPLATE,
                "&e" + kitName,
                lore
            ));
        }

        // Back Button
        inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
    }
}

package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.utils.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class KitEditMenu {

    private final DkKlan plugin;
    private final String kitName;

    public KitEditMenu(DkKlan plugin, String kitName) {
        this.plugin = plugin;
        this.kitName = kitName;
    }

    public void open(Player player) {
        // 54 slots: 
        // 0-35: Inventory
        // 36-39: Armor (Boots, Leggings, Chest, Helmet)
        // 40: Offhand
        // 41-52: Spacer
        // 53: Save
        Inventory inv = Bukkit.createInventory(null, 54, "§8Kit Düzenle: " + kitName);

        File file = new File(plugin.getDataFolder(), "kits/" + kitName + ".yml");
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Load Inventory (0-35)
            List<ItemStack> contentList = (List<ItemStack>) config.getList("inventory");
            if (contentList != null) {
                for (int i = 0; i < contentList.size() && i < 36; i++) {
                    inv.setItem(i, contentList.get(i));
                }
            }
            
            // Load Armor (36-39)
            List<ItemStack> armorList = (List<ItemStack>) config.getList("armor");
            if (armorList != null) {
                // Usually armor list is [boots, leggings, chest, helmet] or reverse depending on how it was saved.
                // Player.getInventory().getArmorContents() returns [boots, leggings, chest, helmet].
                for (int i = 0; i < armorList.size() && i < 4; i++) {
                    inv.setItem(36 + i, armorList.get(i));
                }
            }
            
            // Load Offhand (40)
            ItemStack offhand = config.getItemStack("offhand");
            if (offhand != null) {
                inv.setItem(40, offhand);
            }
        }

        // Fill spacers
        ItemStack glass = MenuUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 41; i < 53; i++) {
            inv.setItem(i, glass);
        }
        
        // Armor Icons for hints if empty
        if (inv.getItem(36) == null) inv.setItem(36, MenuUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "§7Bot Slotu"));
        if (inv.getItem(37) == null) inv.setItem(37, MenuUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "§7Pantolon Slotu"));
        if (inv.getItem(38) == null) inv.setItem(38, MenuUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "§7Göğüslük Slotu"));
        if (inv.getItem(39) == null) inv.setItem(39, MenuUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "§7Kask Slotu"));
        if (inv.getItem(40) == null) inv.setItem(40, MenuUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "§7Offhand Slotu"));

        // Save Button
        inv.setItem(53, MenuUtils.createItem(Material.LIME_CONCRETE, "§a§lKAYDET", "§7Değişiklikleri kaydetmek için tıkla."));

        player.openInventory(inv);
    }
}

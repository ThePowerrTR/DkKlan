package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.TutorialManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class PopupMenu {
    private final DkKlan plugin;
    public PopupMenu(DkKlan plugin) {
        this.plugin = plugin;
    }
    public void open(Player player, String key, String title, List<String> lines) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Tutorial");
        
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER, title, lines));
        inv.setItem(30, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.LIME_CONCRETE, "&aAnladım"));
        inv.setItem(32, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.RED_CONCRETE, "&cBir daha gösterme"));
        
        TutorialManager.get(plugin).setPending(player.getUniqueId(), key);
        player.openInventory(inv);
    }
}

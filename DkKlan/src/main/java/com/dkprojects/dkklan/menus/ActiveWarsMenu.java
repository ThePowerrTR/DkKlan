package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.war.War;
import com.dkprojects.dkklan.managers.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActiveWarsMenu {

    private final DkKlan plugin;

    public ActiveWarsMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Aktif Savaşlar");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.RED_STAINED_GLASS_PANE);

        WarManager wm = plugin.getWarManager();
        Collection<War> wars = wm.getActiveWars();

        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (War war : wars) {
            if (index >= slots.length) break;

            inv.setItem(slots[index++], com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.DIAMOND_SWORD,
                "&c" + war.getClanA() + " vs " + war.getClanB(),
                "",
                "&7Arena: &e" + war.getArena().getName(),
                "&7Durum: &a" + war.getState().name(),
                "&7Skor: &6" + war.getScoreA() + " - " + war.getScoreB(),
                "",
                "&e▶ İZLEMEK İÇİN TIKLA"
            ));
        }

        inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        p.openInventory(inv);
    }
}

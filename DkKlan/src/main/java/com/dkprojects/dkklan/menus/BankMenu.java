
package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class BankMenu {

    private final DkKlan plugin;

    public BankMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (klanName == null) return;

        Inventory inv = Bukkit.createInventory(null, 45, "§8Klan Bankası");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Balance Info
        double balance = plugin.getKlanManager().getKlanBank(klanName);
        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.SUNFLOWER,
            "&6&lBanka Bakiyesi",
            "",
            "&7Mevcut Bakiye:",
            "&6" + balance + " DkCoin",
            ""
        ));

        // Deposit Items
        inv.setItem(19, createTransactionItem(Material.LIME_STAINED_GLASS_PANE, "&aYatır: 1,000 DkCoin", 1000));
        inv.setItem(20, createTransactionItem(Material.LIME_STAINED_GLASS_PANE, "&aYatır: 10,000 DkCoin", 10000));
        inv.setItem(21, createTransactionItem(Material.LIME_STAINED_GLASS_PANE, "&aYatır: 100,000 DkCoin", 100000));

        // Withdraw Items
        inv.setItem(23, createTransactionItem(Material.RED_STAINED_GLASS_PANE, "&cÇek: 1,000 DkCoin", 1000));
        inv.setItem(24, createTransactionItem(Material.RED_STAINED_GLASS_PANE, "&cÇek: 10,000 DkCoin", 10000));
        inv.setItem(25, createTransactionItem(Material.RED_STAINED_GLASS_PANE, "&cÇek: 100,000 DkCoin", 100000));

        // Back Button
        inv.setItem(40, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    private ItemStack createTransactionItem(Material mat, String name, int amount) {
        return com.dkprojects.dkklan.utils.MenuUtils.createItem(mat, name, "", "&7İşlem tutarı: &e" + amount, "");
    }
}

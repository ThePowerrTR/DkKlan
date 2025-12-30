package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WarInfoMenu {

    private DkKlan plugin;

    public WarInfoMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Klan Savaşı Bilgi");

        // Fillers
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Info Item 1: General Info & Commands
        inv.setItem(11, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BOOK,
            "&e&lGenel Bilgiler ve Komutlar",
            "&7",
            "&fKlan Savaşları &7sayesinde gücünüzü",
            "&7diğer klanlara kanıtlayabilirsiniz.",
            "&7",
            "&6Komutlar:",
            "&8▪ &f/klansavasi savas davet <Klan> [Bahis]",
            "&8▪ &f/klansavasi savas kabul <Klan>",
            "&8▪ &f/klansavasi izle <Klan>"
        ));

        // Info Item 2: Rules
        inv.setItem(13, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
            "&c&lKurallar",
            "&7",
            "&8▪ &7Savaşlar &b3v3 &7yapılır.",
            "&8▪ &7Süre sınırı &c30 dakikadır&7.",
            "&8▪ &7En az &c5 aktif üye &7gerekir.",
            "&8▪ &aKeepInventory &7açıktır (Eşya düşmez).",
            "&8▪ &7En çok öldürme alan kazanır."
        ));

        // Info Item 3: Rewards
        inv.setItem(15, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.GOLD_INGOT,
            "&a&lÖdüller",
            "&7",
            "&8▪ &6Klan ELO Puanı",
            "&8▪ &aBahis Ödülü &7(Varsa)",
            "&8▪ &dMVP Ödülü &7(En iyi oyuncuya)"
        ));

        // Close Button
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER, "&cKapat"));

        player.openInventory(inv);
    }
}

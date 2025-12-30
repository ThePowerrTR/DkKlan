package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AcceptInviteMenu {

    private final DkKlan plugin;
    private final String senderClan;
    private final double bet;
    private final int playerCount;
    private final int bestOf;
    private final String kitName;

    public AcceptInviteMenu(DkKlan plugin, String senderClan, double bet, int playerCount, int bestOf, String kitName) {
        this.plugin = plugin;
        this.senderClan = senderClan;
        this.bet = bet;
        this.playerCount = playerCount;
        this.bestOf = bestOf;
        this.kitName = kitName;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Savaş Daveti");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLUE_STAINED_GLASS_PANE);

        inv.setItem(13, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
            "&6&lDavet Özeti",
            "&7Rakip: &e" + senderClan,
            "&7Format: &f" + (bestOf > 1 ? ("BO" + bestOf) : "Tek Maç"),
            "&7Oyuncu: &f" + (playerCount > 0 ? (playerCount + "v" + playerCount) : "Tüm üyeler"),
            "&7Bahis: &a" + bet,
            "&7Kit: &f" + (kitName != null ? kitName : "-"),
            "",
            "&7Bu davet 30 saniye içinde geçerlidir."
        ));

        inv.setItem(11, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.LIME_CONCRETE, "&aKabul Et"));
        inv.setItem(15, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.RED_CONCRETE, "&cReddet"));

        player.openInventory(inv);
    }
}

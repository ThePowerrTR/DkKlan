package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class UpgradesMenu {

    private final DkKlan plugin;

    public UpgradesMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (klanName == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, "§8Klan Yükseltmeleri");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.PURPLE_STAINED_GLASS_PANE);

        int memberLevel = plugin.getKlanManager().getMemberLimitLevel(klanName);
        int currentMemberLimit = plugin.getKlanManager().calculateMemberLimit(memberLevel);
        double memberUpgradeCost = plugin.getKlanManager().getNextMemberLimitCost(memberLevel);

        // Member Limit Upgrade
        inv.setItem(11, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PLAYER_HEAD,
            "&a&lÜye Limiti Artır",
            "",
            "&7Klan üye limitini +5 artırır.",
            "&7Mevcut Seviye: &e" + memberLevel,
            "&7Mevcut Limit: &e" + currentMemberLimit,
            "",
            "&7Ücret: &6" + String.format("%,.0f", memberUpgradeCost) + " TL",
            "",
            "&eSatın almak için tıkla!"
        ));

        int bankLevel = plugin.getKlanManager().getBankLimitLevel(klanName);
        double currentBankLimit = plugin.getKlanManager().calculateBankLimit(bankLevel);
        double bankUpgradeCost = plugin.getKlanManager().getNextBankLimitCost(bankLevel);

        // Bank Limit Upgrade
        inv.setItem(15, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.GOLD_BLOCK,
            "&6&lBanka Limiti Artır",
            "",
            "&7Klan banka limitini +100k artırır.",
            "&7Mevcut Seviye: &e" + bankLevel,
            "&7Mevcut Limit: &e" + String.format("%,.0f", currentBankLimit),
            "",
            "&7Ücret: &6" + String.format("%,.0f", bankUpgradeCost) + " TL",
            "",
            "&eSatın almak için tıkla!"
        ));

        // Back Button
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }
}

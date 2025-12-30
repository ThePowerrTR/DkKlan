package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.ClanSeasonStats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SeasonMenu {

    private final DkKlan plugin;

    public SeasonMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Sezon Tablosu ve Sıralama");

        // Background
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.ORANGE_STAINED_GLASS_PANE);

        // Top Clans (Sorted by Points)
        List<ClanSeasonStats> topClans = plugin.getSeasonManager().getTopClans(10);
        
        int[] slots = {13, 21, 22, 23, 29, 30, 31, 32, 33}; // Top 9 positions
        int index = 0;
        
        for (ClanSeasonStats stat : topClans) {
            if (index >= slots.length) break;
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sMeta = (SkullMeta) head.getItemMeta();
            sMeta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&6#" + (index + 1) + " " + stat.getClanName()));
            
            sMeta.setLore(Arrays.asList(
                "",
                org.bukkit.ChatColor.translateAlternateColorCodes('&', "&7Puan: &e" + stat.getPoints()),
                org.bukkit.ChatColor.translateAlternateColorCodes('&', "&7Galibiyet: &a" + stat.getWins()),
                org.bukkit.ChatColor.translateAlternateColorCodes('&', "&7Mağlubiyet: &c" + stat.getLosses()),
                org.bukkit.ChatColor.translateAlternateColorCodes('&', "&7MVP Sayısı: &6" + stat.getMvpCount()),
                org.bukkit.ChatColor.translateAlternateColorCodes('&', "&7Kazanma Oranı: &d%" + String.format("%.1f", stat.getWinRate()))
            ));
            
            head.setItemMeta(sMeta);
            inv.setItem(slots[index], head);
            index++;
        }
        
        // Header
        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.NETHER_STAR,
            "&6&lSEZON SIRALAMASI",
            "&7En yüksek puanlı klanlar",
            "&7burada listelenir.",
            "",
            "&eSıralama Puanı:",
            "&7Galibiyet: &a+10",
            "&7BO3 Galibiyet: &a+20",
            "&7BO5 Galibiyet: &a+35",
            "&7Turnuva: &a+50/100"
        ));

        // Close Button
        inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER, "&cKapat"));
        
        // Player's Clan Stats
        String playerClan = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (playerClan != null) {
            ClanSeasonStats myStats = plugin.getSeasonManager().getStats(playerClan);
            inv.setItem(40, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.DRAGON_EGG,
                "&b&lSenin Klanın: " + playerClan,
                "",
                "&7Puan: &e" + myStats.getPoints(),
                "&7Galibiyet: &a" + myStats.getWins(),
                "&7Mağlubiyet: &c" + myStats.getLosses(),
                "&7MVP Sayısı: &6" + myStats.getMvpCount(),
                "&7Kazanma Oranı: &d%" + String.format("%.1f", myStats.getWinRate())
            ));
        }

        player.openInventory(inv);
    }
}

package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.WarManager;
import com.dkprojects.dkklan.war.War;
import com.dkprojects.dkklan.war.WarState;
import com.dkprojects.dkklan.war.WarTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WarRosterMenu {

    private final DkKlan plugin;

    public WarRosterMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String clan = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klanda değilsin!");
            return;
        }

        War war = plugin.getWarManager().getWar(clan);
        if (war == null || war.getState() != WarState.PREPARING) {
            player.sendMessage("§cŞu an kadro seçimi yapılan bir savaş yok.");
            return;
        }

        if (war.getTargetPlayerCount() <= 0) {
            player.sendMessage("§cBu savaşta kadro seçimi gerekmiyor (Tüm üyeler).");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§8Kadro Yönetimi");

        // Background
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Info
        WarTeam team = war.getTeamA().getClanName().equals(clan) ? war.getTeamA() : war.getTeamB();
        int current = team.getPlayers().size();
        int target = war.getTargetPlayerCount();

        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
            "&e&lKadro Durumu",
            "&7Seçilen: &f" + current + "/" + target,
            "",
            "&7Kadro dolduğunda savaş",
            "&7otomatik başlayacaktır."
        ));

        // Members
        List<String> members = plugin.getKlanManager().getKlanMembers(clan);
        int slot = 18;

        for (String id : members) {
            UUID uuid;
            try { uuid = UUID.fromString(id); } catch (Exception e) { continue; }
            Player p = Bukkit.getPlayer(uuid);
            
            // Only online players can be added
            boolean isOnline = p != null && p.isOnline();
            boolean isInRoster = team.getPlayers().contains(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (isOnline) meta.setOwningPlayer(p);
            
            String name = isOnline ? p.getName() : "&7(Çevrimdışı) " + id; // Fallback if offline logic needed, but likely not
            if (!isOnline) {
                // Try to get offline name
                name = Bukkit.getOfflinePlayer(uuid).getName();
                if (name == null) name = "Bilinmiyor";
            }

            List<String> lore = new ArrayList<>();
            if (isInRoster) {
                meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&a" + name));
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                lore.add("&7Durum: &aKadroda");
                if (isOnline) lore.add("&eÇıkarmak için tıkla");
            } else {
                meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', isOnline ? "&e" + name : "&c" + name));
                lore.add("&7Durum: &cKadroda Değil");
                if (isOnline) {
                     if (current < target) lore.add("&aEklemek için tıkla");
                     else lore.add("&cKadro dolu");
                } else {
                    lore.add("&cÇevrimdışı");
                }
            }
            
            // Translate lore colors
            List<String> coloredLore = new ArrayList<>();
            for (String l : lore) coloredLore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', l));
            meta.setLore(coloredLore);
            
            head.setItemMeta(meta);
            inv.setItem(slot++, head);
            if (slot > 53) break;
        }

        // Back
        inv.setItem(45, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
    }
}

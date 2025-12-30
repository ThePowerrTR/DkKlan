package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.WarArena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarArenasMenu {

    private final DkKlan plugin;

    public WarArenasMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Arenalar");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Info
        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
            "&e&lArena Listesi",
            "&7Sunucudaki tüm savaş",
            "&7arenalarının durumu."
        ));

        // List Arenas
        int slot = 18;
        for (WarArena arena : plugin.getWarManager().getArenas()) {
            boolean inUse = arena.isInUse();
            boolean isSetup = arena.isSetup();
            
            Material mat = inUse ? Material.RED_CONCRETE : (isSetup ? Material.LIME_CONCRETE : Material.YELLOW_CONCRETE);
            String name = "&a" + arena.getName();
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Durum: " + (inUse ? "&cSavaşta" : (isSetup ? "&aMüsait" : "&eHazırlanıyor")));
            lore.add("&7Kapasite: &f" + arena.getMinPlayers() + "-" + arena.getMaxPlayers());
            lore.add("&7Boyut: &f" + arena.getSize());
            
            if (player.hasPermission("dkklan.admin")) {
                lore.add("");
                lore.add("&eYönetmek için tıkla");
            }
            
            inv.setItem(slot++, com.dkprojects.dkklan.utils.MenuUtils.createItem(mat, name, lore));
            
            if (slot > 53) break;
        }

        // Create Button (Admin)
        if (player.hasPermission("dkklan.admin")) {
            inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ANVIL,
                "&a&lYENİ ARENA OLUŞTUR",
                "&7Komut: /klansavasi arena olustur <isim>"
            ));
        }

        // Back
        inv.setItem(45, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
    }
}

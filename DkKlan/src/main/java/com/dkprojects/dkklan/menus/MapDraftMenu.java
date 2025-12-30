package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.war.MapDraft;
import com.dkprojects.dkklan.war.MatchSeries;
import com.dkprojects.dkklan.objects.WarArena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class MapDraftMenu {

    private final DkKlan plugin;
    private final MapDraft draft;

    public MapDraftMenu(DkKlan plugin, MapDraft draft) {
        this.plugin = plugin;
        this.draft = draft;
    }

    public void open(Player player) {
        String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (klanName == null) return;
        
        if (draft == null) {
            player.sendMessage("§cŞu an aktif bir draft yok.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§8Harita Draftı §c❗ Yanlış tıklama iptal edilemez");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.CYAN_STAINED_GLASS_PANE);

        // Info Item
        String turnStatus = draft.isFinished() ? "&aTamamlandı" : 
                           (draft.getCurrentTurn().equals(klanName) ? "&aSİZİN SIRANIZ" : "&cRAKİP BEKLENİYOR");
        
        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
            "&e&lDRAFT DURUMU",
            "",
            "&7Sıra: " + turnStatus,
            "&7Klan: &f" + (draft.isFinished() ? "-" : draft.getCurrentTurn()),
            "",
            "&7Yeşil: &aSeçilebilir",
            "&7Kırmızı: &cYasaklandı",
            "",
            "&c⚠ DİKKAT: Yanlış tıklama iptal edilemez!"
        ));

        // Active Maps (Pool)
        int slot = 10;
        int[] poolSlots = {10, 11, 12, 13, 14, 15, 16};
        int poolIndex = 0;

        for (WarArena arena : draft.getPool()) {
             if (poolIndex >= poolSlots.length) break;
             
             inv.setItem(poolSlots[poolIndex++], com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.MAP,
                "&a" + arena.getName(),
                "",
                "&7Durum: &aOynanabilir",
                "",
                draft.isFinished() ? "&eSıraya alındı" : "&cYasaklamak için tıkla!"
             ));
        }

        // Banned Maps
        int[] banSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int banIndex = 0;
        
        for (WarArena arena : draft.getBannedMaps()) {
            if (banIndex >= banSlots.length) break;
            
            inv.setItem(banSlots[banIndex++], com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER,
                "&c" + arena.getName(),
                "",
                "&7Durum: &cYasaklandı",
                "&7Bu seride oynanmayacak."
            ));
        }

        // Back Button
        inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
    }
}

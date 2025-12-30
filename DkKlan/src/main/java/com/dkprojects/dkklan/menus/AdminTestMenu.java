package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.utils.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class AdminTestMenu {
    private final DkKlan plugin;
    public AdminTestMenu(DkKlan plugin) { this.plugin = plugin; }
    
    public void open(Player player) {
        if (!player.hasPermission("dkklan.test")) return;
        Inventory inv = Bukkit.createInventory(null, 45, "§8Admin Test Paneli");
        
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLACK_STAINED_GLASS_PANE);
        
        boolean enabled = plugin.getWarManager().isTestModeEnabled();
        
        inv.setItem(10, com.dkprojects.dkklan.utils.MenuUtils.createItem(
            enabled ? Material.REDSTONE_TORCH : Material.LEVER,
            "&c&lTEST MODU: " + (enabled ? "AÇIK" : "KAPALI"),
            "&7Tıklayarak değiştir"
        ));

        inv.setItem(12, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.NAME_TAG, "&a&lSAHTE KLAN OLUŞTUR", "&7Otomatik isim ile sahte klan"));
        inv.setItem(14, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ZOMBIE_HEAD, "&b&lBOT EKLE (KLANIM)", "&7Varsayılan adet ile ekler"));
        inv.setItem(20, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.MAP, "&e&lDRAFTI BİTİR (KLANIM)", "&7Seriyi başlatır"));
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER, "&e&lDRAFTTAN BAŞLAT (KLANIM)", "&7Mevcut seriyi başlatır"));
        inv.setItem(24, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.DIAMOND_SWORD, "&6&lKILL EKLE (KLANIM)"));
        inv.setItem(30, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.GOLD_INGOT, "&6&lSKOR +1 (KLANIM)"));
        inv.setItem(32, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER, "&c&lSAVAŞI BİTİR (KLANIM)"));
        
        inv.setItem(40, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));
        
        player.openInventory(inv);
        SoundManager.play(player, SoundManager.Key.MENU_OPEN);
    }
    
    public static String generateFakeClanName() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return "FAKE_" + sb.toString();
    }
}

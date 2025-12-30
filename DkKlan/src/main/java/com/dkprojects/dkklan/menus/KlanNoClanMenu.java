package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KlanNoClanMenu {
    private DkKlan plugin;

    public KlanNoClanMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Klan Menüsü");

        // Background filler
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.GRAY_STAINED_GLASS_PANE);

        // Klan Kur (Slot 20)
        inv.setItem(20, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ANVIL,
            "&a&lKlan Kur",
            "",
            "&7Yeni bir klan kurmak",
            "&7için tıkla.",
            ""
        ));

        // Klanları Listele (Slot 22)
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BOOKSHELF,
            "&e&lKlanları Listele",
            "",
            "&7Sunucudaki klanları",
            "&7görmek için tıkla.",
            ""
        ));
        
        // Davetleri Gör (Slot 24)
        inv.setItem(24, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
            "&b&lDavetleri Gör",
            "",
            "&7Sana gelen klan davetlerini",
            "&7görmek için tıkla.",
            ""
        ));

        // Bilgi (Slot 40)
        inv.setItem(40, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BOOK,
            "&e&lBilgi",
            "",
            "&7Klan komutlarını",
            "&7görmek için tıkla.",
            ""
        ));

        // Kapat (Slot 44)
        inv.setItem(44, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER, "&cKapat"));

        player.openInventory(inv);
        com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
    }
}

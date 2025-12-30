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

public class RulesMenu {

    private final DkKlan plugin;

    public RulesMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Klan Savaşı Kuralları");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.ORANGE_STAINED_GLASS_PANE);

        // 1. Genel Kurallar
        inv.setItem(10, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
            "&e&l1. GENEL KURALLAR",
            "&7▪ &fEşitlik Garantisi: &7Herkes eşit şartlarda başlar.",
            "&7▪ &fSabit Kit: &7Kendi eşyalarınla savaşa giremezsin.",
            "&7▪ &fRound Reset: &7Her round full can/açlık ve sıfır kit.",
            "&7▪ &fBO3/BO5: &7Setler arası eşya taşıma yoktur."
        ));

        // 2. Kadro Kuralları
        inv.setItem(12, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PLAYER_HEAD,
            "&e&l2. KADRO KURALLARI",
            "&7▪ &fKadro Kilidi: &7Hazırlık bitince kadro kilitlenir.",
            "&7▪ &fSub Yasağı: &7Savaş başladıktan sonra oyuncu değişmez.",
            "&7▪ &fÇıkış Cezası: &7Çıkan oyuncu geri giremez.",
            "&7▪ &fHükmen Mağlubiyet: &7Tüm takım çıkarsa kaybeder."
        ));

        // 3. Kit Kuralları
        inv.setItem(14, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.IRON_CHESTPLATE,
            "&e&l3. KİT KURALLARI",
            "&7▪ &fKit Seçimi: &7Savaş öncesi belirlenir ve kilitlenir.",
            "&7▪ &fYasaklar: &7Kit dışı eşya, yere item atma yasaktır.",
            "&7▪ &fOtomatik Engel: &7Sistem yasaklı eylemleri engeller."
        ));

        // 4. Harita & Yayın
        inv.setItem(16, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.MAP,
            "&e&l4. HARİTA & YAYIN",
            "&7▪ &fDraft: &7Ban/Pick sistemi ile harita seçilir.",
            "&7▪ &fYayıncı Modu: &7Sadece yetkililer izleyebilir.",
            "&7▪ &fGhost Yasak: &7Yayıncıdan bilgi almak yasaktır."
        ));

        // 5. Puan Sistemi
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.GOLD_INGOT,
            "&6&l5. PUAN & ÖDÜL",
            "&7▪ &aNormal Galibiyet: &f+10 Puan",
            "&7▪ &aBO3 Galibiyet: &f+20 Puan",
            "&7▪ &aBO5 Galibiyet: &f+35 Puan",
            "&7▪ &aTurnuva Maçı: &f+50 Puan",
            "&7▪ &aŞampiyonluk: &f+100 Puan",
            "&7▪ &bMVP: &f+5 Puan"
        ));

        // Accept Button
        inv.setItem(40, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.EMERALD_BLOCK,
            "&a&lKURALLARI KABUL EDİYORUM",
            "&7Bu kuralları okudum ve kabul ediyorum.",
            "&7Klan savaşına girmek için onaylıyorum.",
            "",
            "&eOnaylamak için tıkla!"
        ));

        // Back
        inv.setItem(36, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
    }
}

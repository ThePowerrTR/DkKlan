package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.WarManager;
import com.dkprojects.dkklan.utils.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WarMenu {

    private final DkKlan plugin;

    public WarMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (klanName == null) {
            player.sendMessage("§cBir klanda değilsin!");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§8Klan Savaşları & E-Spor");

        // Fillers
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.RED_STAINED_GLASS_PANE);

        // --- Buttons ---

        // 1. Savaş Başlat (Slot 20)
        boolean isLeader = plugin.getKlanManager().isLeader(player.getUniqueId());
        boolean rulesAccepted = plugin.getWarManager().hasAcceptedRules(player.getUniqueId());
        boolean canStart = isLeader && rulesAccepted && plugin.getWarManager().getWar(klanName) == null;

        inv.setItem(20, com.dkprojects.dkklan.utils.MenuUtils.createItem(
            canStart ? Material.DIAMOND_SWORD : Material.BARRIER,
            canStart ? "&a&lSAVAŞ BAŞLAT" : "&c&lSAVAŞ BAŞLAT",
            canStart, // glow
            "",
            canStart ? "&7Rakip klanla savaş başlatmak" : "&7Savaş başlatmak için lider olmalı",
            canStart ? "&7için meydan okuyun." : "&7ve kuralları kabul etmelisin.",
            "",
            canStart ? "&a▶ Meydan Oku" : "&cŞartlar Sağlanmadı"
        ));

        // 2. Aktif Savaşlar / İzle (Slot 21 -> 22 Center)
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ENDER_EYE,
            "&b&lCANLI SAVAŞLAR",
            "",
            "&7Şu an devam eden savaşları",
            "&7izlemek için tıklayın.",
            "",
            "&b▶ İzleyici Moduna Geç"
        ));

        // 3. Harita & Draft (Slot 22 -> 24 Right)
        inv.setItem(24, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.MAP,
            "&e&lHARİTA & DRAFT",
            "",
            "&7Harita seçim ve yasaklama",
            "&7ekranını görüntüler.",
            "",
            "&e▶ Durumu Gör"
        ));

        // 4. Kurallar (Slot 23 -> 4)
        // Moved to top for easy access info
        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BOOK,
            "&6&lKURALLAR",
            "",
            "&7Savaş kurallarını ve",
            "&7cezai işlemleri okuyun.",
            "",
            "&6▶ Okumak İçin Tıkla"
        ));

        // 5. Kit Seçimi (Slot 24 -> 38 Bottom Left)
        inv.setItem(38, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.IRON_CHESTPLATE,
            "&b&lKİT SEÇİMİ",
            "",
            "&7Savaşlarda kullanılacak",
            "&7ekipman setlerini incele.",
            "",
            "&b▶ Kitleri Gör"
        ));

        // 6. Kadro (Slot 29 -> 40 Bottom Center)
        inv.setItem(40, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PLAYER_HEAD,
            "&e&lKADRO YÖNETİMİ",
            "",
            "&7Savaş kadrosunu düzenle",
            "&7ve oyuncu seç.",
            "",
            "&e▶ Kadroyu Düzenle"
        ));

        // 7. Sezon Tablosu (Slot 30 -> 42 Bottom Right)
        inv.setItem(42, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.NETHER_STAR,
            "&d&lSEZON TABLOSU",
            "",
            "&7En iyi klanlar ve",
            "&7oyuncu sıralamaları.",
            "",
            "&d▶ Sıralamayı Gör"
        ));

        // 8. Arenalar (Slot 31 -> 49 Center Bottom/Exit or 44)
        // Let's put Arenas at 44 (Bottom Right Corner)
        inv.setItem(44, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.GRASS_BLOCK,
            "&a&lARENALAR",
            "",
            "&7Savaş alanlarını listele",
            "&7ve incele.",
            "",
            "&a▶ Arenaları Gör"
        ));

        // 9. Admin Test (Slot 32 -> 49)
        if (player.hasPermission("dkklan.test")) {
            boolean enabled = plugin.getWarManager().isTestModeEnabled();
            inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(
                enabled ? Material.REDSTONE_TORCH : Material.LEVER,
                "&c&lADMIN TEST MODU",
                "",
                enabled ? "&aDurum: AÇIK" : "&cDurum: KAPALI",
                "&7Otomatik bot savaşlarını",
                "&7tetiklemek için kullan.",
                "",
                "&c▶ Değiştirmek İçin Tıkla"
            ));
        } else {
             // Close Button (Slot 49) if not admin
             inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER, "&cKapat"));
        }

        // --- Status Indicator (Slot 13 - Center Top under Info) ---
        // Moved from 4 to 13 because 4 is now Rules
        
        com.dkprojects.dkklan.war.MatchSeries series = plugin.getWarManager().getSeries(klanName);
        WarManager.Invite invite = plugin.getWarManager().getInviteForClan(klanName); 

        if (series != null) {
            String opponent = series.getClanA().equals(klanName) ? series.getClanB() : series.getClanA();
            int scoreA = series.getScoreA();
            int scoreB = series.getScoreB();
            int myScore = series.getClanA().equals(klanName) ? scoreA : scoreB;
            int oppScore = series.getClanA().equals(klanName) ? scoreB : scoreA;
            
            inv.setItem(13, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.REDSTONE_BLOCK,
                "&c&lAKTİF SAVAŞ DURUMU",
                true,
                "",
                "&7Rakip: &c" + opponent,
                "&7Skor: &e" + myScore + " - " + oppScore,
                "&7Format: &f" + series.getFormat().name(),
                "",
                "&a▶ SAVAŞA DÖNMEK İÇİN TIKLA"
            ));
        } else if (invite != null) {
             inv.setItem(13, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.WRITABLE_BOOK,
                 "&e&lSAVAŞ DAVETİ VAR!",
                 true,
                 "",
                 "&7Gönderen: &e" + invite.senderClan,
                 "&7Bahis: &a" + invite.bet,
                 "&7Format: &fBO" + invite.bestOf,
                 "",
                 "&a▶ KABUL ETMEK İÇİN TIKLA"
             ));
        } else {
            inv.setItem(13, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.LIME_DYE,
                "&a&lDURUM: HAZIR",
                "",
                "&7Herhangi bir savaş veya",
                "&7aktif davet bulunmuyor.",
                "",
                "&7Yeni bir savaş başlatabilirsin."
            ));
        }

        player.openInventory(inv);
    }
}

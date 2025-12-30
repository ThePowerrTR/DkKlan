package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class KlanMenu {

    private final DkKlan plugin;

    public KlanMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (klanName == null) {
            player.sendMessage(plugin.getMessage("not-in-clan"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 45, "§8Klan Yönetimi: " + klanName);

        // Fillers
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLACK_STAINED_GLASS_PANE);
        
        // Klan Info (Center Top - Slot 4)
        int level = plugin.getKlanManager().getKlanLevel(klanName);
        double bank = plugin.getKlanManager().getKlanBank(klanName);
        String leaderUuid = plugin.getKlanManager().getKlanLeader(klanName);
        String leaderName = Bukkit.getOfflinePlayer(UUID.fromString(leaderUuid)).getName();
        List<String> members = plugin.getKlanManager().getMembers(klanName);

        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER, 
            "&e&l" + klanName.toUpperCase() + " KLAN BİLGİSİ",
            "",
            "&7Lider: &f" + (leaderName != null ? leaderName : "Bilinmiyor"),
            "&7Seviye: &6" + level,
            "&7Üye Sayısı: &b" + members.size(),
            "&7Kasa: &a" + bank + " TL",
            "",
            "&eBilgi: &7Klan istatistikleri ve",
            "&7detayları burada görüntülenir."
        ));

        // Members Button (Slot 20)
        inv.setItem(20, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PLAYER_HEAD,
            "&b&lÜYELER",
            "",
            "&7Klan üyelerini görüntüle,",
            "&7yönet ve rütbelerini ayarla.",
            "",
            "&a▶ Tıkla ve Yönet"
        ));

        // War Menu Button (Slot 22)
        inv.setItem(22, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.DIAMOND_SWORD,
            "&c&lKLAN SAVAŞLARI",
            true, // glow
            "",
            "&7Klan savaşları, turnuvalar",
            "&7ve arena yönetim paneli.",
            "",
            "&c▶ Savaş Menüsünü Aç"
        ));

        // Bank Button (Slot 24)
        inv.setItem(24, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.GOLD_INGOT,
            "&6&lKLAN KASASI",
            "",
            "&7Mevcut Bakiye: &a" + bank + " TL",
            "&7Para yatırıp çekebilirsin.",
            "",
            "&e▶ İşlem Yapmak İçin Tıkla"
        ));
        
        // Upgrades Button (Slot 38 - Bottom Leftish)
        inv.setItem(38, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BEACON,
            "&d&lYÜKSELTMELER",
            "",
            "&7Klan seviyesini ve özelliklerini",
            "&7geliştirmek için market.",
            "",
            "&d▶ Yükseltmeleri İncele"
        ));

        // Base Teleport (Slot 40 - Bottom Center)
        if (plugin.getKlanManager().getKlanBase(klanName) != null) {
            inv.setItem(40, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ENDER_PEARL,
                "&b&lKLAN EVİNE GİT",
                "",
                "&7Klan evine ışınlan.",
                "",
                "&b▶ Işınlan"
            ));
        }

        // Admin/Leader Actions
        ClanRole role = plugin.getKlanManager().getRole(player.getUniqueId());
        
        // Set Base (Slot 42 - Bottom Rightish)
        if (role.isAtLeast(ClanRole.ADMIN)) {
            inv.setItem(42, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.COMPASS,
                "&e&lKLAN EVİNİ BELİRLE",
                "",
                "&7Bulunduğun konumu klan evi",
                "&7olarak ayarlar.",
                "",
                "&e▶ Ayarlamak İçin Tıkla"
            ));
        }

        // Invite Player (Slot 10 - Top Left Corner)
        if (role.isAtLeast(ClanRole.ADMIN)) {
            inv.setItem(10, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.WRITABLE_BOOK,
                "&a&lOYUNCU DAVET ET",
                "",
                "&7Klanına yeni bir üye",
                "&7davet etmek için tıkla.",
                "",
                "&a▶ Davet Gönder"
            ));
        }

        // Announcement (Slot 16 - Top Right Corner)
        if (role == ClanRole.LEADER) {
            inv.setItem(16, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.OAK_SIGN,
                "&6&lDUYURU YAP",
                "",
                "&7Tüm klan üyelerine",
                "&7mesaj gönder.",
                "",
                "&6▶ Duyuru Oluştur"
            ));
        }

        // Leave/Disband (Slot 44 - Bottom Right Corner)
        boolean isLeader = role == ClanRole.LEADER;
        if (isLeader) {
             inv.setItem(44, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.TNT,
                "&c&lKLANI DAĞIT",
                "",
                "&7Klanı kalıcı olarak siler.",
                "&7Bu işlem geri alınamaz!",
                "",
                "&c⚠ DİKKAT: Tehlikeli İşlem"
            ));
        } else {
             inv.setItem(44, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.RED_DYE,
                "&c&lKLANDAN AYRIL",
                "",
                "&7Klandan ayrılmak için tıkla.",
                "",
                "&c▶ Ayrıl"
            ));
        }
        
        // Close Button (Slot 36 - Bottom Left Corner) is handled by border usually, but let's put explicit close or back?
        // Actually border is fine, but standard close is usually 49 or similar.
        // Let's put a close button at 36 if not used? No, 36 is border.
        // Let's put close at 8 (Top Right) or just rely on ESC.
        // Standard close button at 36 (Bottom Left) replacing border?
        // Let's stick to no close button, ESC is standard. Or put it at 43 if empty?
        // The previous menu had it at 44.
        
        player.openInventory(inv);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }
}

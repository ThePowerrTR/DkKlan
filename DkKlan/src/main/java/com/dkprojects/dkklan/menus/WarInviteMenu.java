package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WarInviteMenu {

    private final DkKlan plugin;

    public WarInviteMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Savaş Davet Menüsü");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.RED_STAINED_GLASS_PANE);

        WarManager.InviteDraft draft = plugin.getWarManager().getInviteDraft(player.getUniqueId());
        if (draft == null) {
            draft = plugin.getWarManager().createInviteDraft(player.getUniqueId());
        }

        // Format Selection
        addOption(inv, 10, Material.PAPER, "&eFormat: BO1", Arrays.asList("&7Tıkla: BO1"), draft.bestOf == 1);
        addOption(inv, 11, Material.PAPER, "&eFormat: BO3", Arrays.asList("&7Tıkla: BO3"), draft.bestOf == 3);
        addOption(inv, 12, Material.PAPER, "&eFormat: BO5", Arrays.asList("&7Tıkla: BO5"), draft.bestOf == 5);

        // Player Count
        addOption(inv, 14, Material.PLAYER_HEAD, "&eOyuncu: Tüm üyeler", Arrays.asList("&7Tıkla: Tüm üyeler"), draft.playerCount <= 0);
        addOption(inv, 15, Material.PLAYER_HEAD, "&eOyuncu: 3v3", Arrays.asList("&7Tıkla: 3v3"), draft.playerCount == 3);
        addOption(inv, 16, Material.PLAYER_HEAD, "&eOyuncu: 5v5", Arrays.asList("&7Tıkla: 5v5"), draft.playerCount == 5);
        addOption(inv, 17, Material.NAME_TAG, "&eOyuncu: Özel Sayı", Arrays.asList("&7Tıkla: İstediğin sayıyı gir", "&7Şu an: " + (draft.playerCount > 0 && draft.playerCount != 3 && draft.playerCount != 5 ? draft.playerCount : "Seçili değil")), draft.playerCount > 0 && draft.playerCount != 3 && draft.playerCount != 5);


        // Bet Selection (if allowed)
        boolean betAllowed = plugin.getRulesetManager().getDefaultRuleset().isBetAllowed();
        if (betAllowed) {
            addOption(inv, 19, Material.GOLD_NUGGET, "&eBahis: 0", Arrays.asList("&7Tıkla: 0"), draft.bet == 0);
            addOption(inv, 20, Material.GOLD_INGOT, "&eBahis: 100000", Arrays.asList("&7Tıkla: 100k"), draft.bet == 100000);
            addOption(inv, 21, Material.GOLD_BLOCK, "&eBahis: 250000", Arrays.asList("&7Tıkla: 250k"), draft.bet == 250000);
            addOption(inv, 22, Material.EMERALD, "&eBahis: 500000", Arrays.asList("&7Tıkla: 500k"), draft.bet == 500000);
            addOption(inv, 23, Material.DIAMOND, "&eBahis: 1000000", Arrays.asList("&7Tıkla: 1M"), draft.bet == 1000000);
        } else {
            inv.setItem(19, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER,
                "&cBahisli Savaş Kapalı",
                "&7Aktif kurallar bahisli savaşı desteklemiyor."
            ));
        }

        // Target Clan Selection (Online clans only)
        Set<String> onlineClans = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String c = plugin.getKlanManager().getPlayerKlan(p.getUniqueId());
            if (c != null) onlineClans.add(c);
        }
        
        // Add Fake Clans if Test Mode
        if (plugin.getWarManager().isTestModeEnabled()) {
            onlineClans.addAll(plugin.getKlanManager().getFakeClans());
        }
        
        int slot = 28;
        for (String clan : onlineClans) {
            boolean unavailable = plugin.getWarManager().getWar(clan) != null;
            boolean isSelected = draft.targetClan != null && draft.targetClan.equals(clan);
            String name = (isSelected ? "&a" : "&e") + "Rakip: " + clan;
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(unavailable ? "&7Durum: &cSavaşta (seçilemez)" : "&7Durum: &aMüsait");
            if (plugin.getKlanManager().isFakeClan(clan)) {
                lore.add("&8&o(Sahte Klan - Test Modu)");
            }
            
            inv.setItem(slot++, com.dkprojects.dkklan.utils.MenuUtils.createItem(
                unavailable ? Material.GRAY_DYE : Material.SHIELD,
                name,
                lore
            ));
            
            if (slot > 33) break;
        }

        // Kit info (selected via Kit Seçim Menüsü)
        String kitName = draft.kitName != null ? draft.kitName : "Seçilmedi";
        inv.setItem(37, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.IRON_CHESTPLATE,
            "&eKit: " + kitName,
            "&7Kit belirlemek için Ana Menü > Kit Seçim"
        ));

        // Summary and Send
        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.WRITABLE_BOOK,
            "&6&lÖzet",
            "&7Rakip: &f" + (draft.targetClan != null ? draft.targetClan : "-"),
            "&7Format: &f" + (draft.bestOf > 1 ? ("BO" + draft.bestOf) : "Tek Maç"),
            "&7Oyuncu: &f" + (draft.playerCount > 0 ? (draft.playerCount + "v" + draft.playerCount) : "Tüm üyeler"),
            "&7Bahis: &a" + draft.bet,
            "&7Kit: &f" + kitName
        ));

        inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.EMERALD_BLOCK, "&a&lDAVET GÖNDER"));

        // Back
        inv.setItem(45, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        player.openInventory(inv);
    }

    private void addOption(Inventory inv, int slot, Material mat, String name, List<String> lore, boolean selected) {
        List<String> l = new ArrayList<>(lore);
        l.add(selected ? "&aSeçili" : "&7Tıklayarak seç");
        inv.setItem(slot, com.dkprojects.dkklan.utils.MenuUtils.createItem(mat, name, l));
    }
}

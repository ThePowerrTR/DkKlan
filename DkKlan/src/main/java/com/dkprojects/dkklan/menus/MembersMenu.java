package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MembersMenu {

    private final DkKlan plugin;

    public MembersMenu(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        if (klanName == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, "§8Klan Üyeleri");

        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.CYAN_STAINED_GLASS_PANE);

        List<String> memberUuids = new ArrayList<>(plugin.getKlanManager().getMembers(klanName));
        
        // Sorting: Leader > Admin > Moderator > Member
        memberUuids.sort((uuid1, uuid2) -> {
            ClanRole r1 = plugin.getKlanManager().getRole(UUID.fromString(uuid1));
            ClanRole r2 = plugin.getKlanManager().getRole(UUID.fromString(uuid2));
            return Integer.compare(r2.getWeight(), r1.getWeight()); // Descending
        });
        
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        boolean isViewerLeader = plugin.getKlanManager().isLeader(player.getUniqueId());
        
        for (String uuidStr : memberUuids) {
            if (index >= slots.length) break;

            UUID uuid = UUID.fromString(uuidStr);
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
            ClanRole role = plugin.getKlanManager().getRole(uuid);
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setOwningPlayer(member);
            headMeta.setDisplayName("§e" + member.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Rol: " + role.getDisplayName());
            lore.add("§7Durum: " + (member.isOnline() ? "§aÇevrimiçi" : "§cÇevrimdışı"));
            lore.add("");
            
            if (isViewerLeader && role != ClanRole.LEADER) {
                lore.add("§eSol Tık: §aRütbe Yükselt");
                lore.add("§cSağ Tık: §4Üyeyi At");
            }
            
            headMeta.setLore(lore);
            head.setItemMeta(headMeta);
            inv.setItem(slots[index++], head);
        }

        // Back Button
        inv.setItem(49, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ARROW, "&cGeri Dön"));

        // Invite Button (Admin+)
        if (plugin.getKlanManager().getRole(player.getUniqueId()).isAtLeast(ClanRole.ADMIN)) {
            inv.setItem(50, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.PAPER,
                "&a&lOYUNCU DAVET ET",
                "&7Klana yeni üye davet et."
            ));
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }
}

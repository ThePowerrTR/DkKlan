package com.dkprojects.dkklan.menus;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.WarArena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class SpectatorMenu {

    private final DkKlan plugin;
    private final WarArena arena;

    public SpectatorMenu(DkKlan plugin, WarArena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8Yayıncı Kamerası: " + arena.getName());

        // Background
        com.dkprojects.dkklan.utils.MenuUtils.fillBorders(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Standard Points
        addPoint(inv, 10, "&aSpawn 1 (Klan A)", arena.getSpawn1());
        addPoint(inv, 12, "&bLobi", arena.getLobby());
        addPoint(inv, 14, "&cSpawn 2 (Klan B)", arena.getSpawn2());
        
        // Custom Camera Points
        int slot = 19;
        for (Map.Entry<String, Location> entry : arena.getCameraPoints().entrySet()) {
            if (slot >= 35) break;
            addPoint(inv, slot++, "&e" + entry.getKey(), entry.getValue());
        }

        // Info
        inv.setItem(4, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.COMPASS,
            "&6&lKAMERA KONTROL",
            "&7İzlemek istediğin noktaya",
            "&7gitmek için tıkla.",
            "",
            "&eYayıncı Modu Aktif"
        ));
        
        // Close
        inv.setItem(31, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.BARRIER, "&cKapat"));

        player.openInventory(inv);
    }
    
    private void addPoint(Inventory inv, int slot, String name, Location loc) {
        if (loc == null) return;
        
        inv.setItem(slot, com.dkprojects.dkklan.utils.MenuUtils.createItem(Material.ENDER_PEARL,
            name,
            "&7X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ(),
            "",
            "&eIşınlanmak için tıkla!"
        ));
    }
}

package com.dkprojects.dkklan.utils;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MenuAnimator {
    public static void phasedOpen(DkKlan plugin, Player player, Inventory inv, Runnable phase1, Runnable phase2, Runnable phase3) {
        if (phase1 != null) phase1.run();
        player.openInventory(inv);
        if (phase2 != null) {
            Bukkit.getScheduler().runTaskLater(plugin, phase2, 5L);
        }
        if (phase3 != null) {
            Bukkit.getScheduler().runTaskLater(plugin, phase3, 10L);
        }
    }
}

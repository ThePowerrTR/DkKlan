package com.dkprojects.dkklan.listeners;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class KlanListener implements Listener {
    private DkKlan plugin;

    public KlanListener(DkKlan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Klan verilerini yükleme veya cache kontrolü (şimdilik manager'da yükleniyor)
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getWarManager().isAwaitingPlayerCount(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getWarManager().setAwaitingPlayerCount(player.getUniqueId(), false);

            if (event.getMessage().equalsIgnoreCase("iptal")) {
                player.sendMessage("§cİşlem iptal edildi.");
                new com.dkprojects.dkklan.menus.WarInviteMenu(plugin).open(player);
                return;
            }

            try {
                int count = Integer.parseInt(event.getMessage());
                if (count < 1) {
                    player.sendMessage("§cOyuncu sayısı en az 1 olmalıdır.");
                    new com.dkprojects.dkklan.menus.WarInviteMenu(plugin).open(player);
                    return;
                }
                if (count > 50) {
                     player.sendMessage("§cÇok yüksek bir sayı girdiniz! Maksimum 50.");
                     new com.dkprojects.dkklan.menus.WarInviteMenu(plugin).open(player);
                     return;
                }
                
                plugin.getWarManager().setDraftPlayerCount(player.getUniqueId(), count);
                player.sendMessage("§aOyuncu sayısı ayarlandı: " + count + "v" + count);
                new com.dkprojects.dkklan.menus.WarInviteMenu(plugin).open(player);
            } catch (NumberFormatException e) {
                player.sendMessage("§cGeçersiz sayı! Lütfen tekrar deneyin.");
                new com.dkprojects.dkklan.menus.WarInviteMenu(plugin).open(player);
            }
            return;
        }
        
        if (plugin.getKlanManager().isInviteMode(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getKlanManager().setInviteMode(player.getUniqueId(), false);
            
            if (event.getMessage().equalsIgnoreCase("iptal")) {
                player.sendMessage("§cDavet işlemi iptal edildi.");
                return;
            }
            
            player.performCommand("klan davet " + event.getMessage());
             return;
         }
         
         if (plugin.getKlanManager().isCreateClanMode(player.getUniqueId())) {
             event.setCancelled(true);
             plugin.getKlanManager().setCreateClanMode(player.getUniqueId(), false);
             
             if (event.getMessage().equalsIgnoreCase("iptal")) {
                 player.sendMessage("§cKlan kurma işlemi iptal edildi.");
                 return;
             }
             
             player.performCommand("klan kur " + event.getMessage());
             return;
         }

         if (plugin.getKlanManager().isAnnouncementMode(player.getUniqueId())) {
             event.setCancelled(true);
             plugin.getKlanManager().setAnnouncementMode(player.getUniqueId(), false);
 
             if (event.getMessage().equalsIgnoreCase("iptal")) {
                 player.sendMessage("§cDuyuru işlemi iptal edildi.");
                 return;
             }
 
             player.performCommand("klan duyuru " + event.getMessage());
             return;
         }
 
          String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
        
        if (klanName != null) {
            // Check if chat toggled
            if (plugin.getKlanManager().isInClanChat(player.getUniqueId())) {
                event.setCancelled(true);
                String msg = "§8[§6Klan§8] §e" + player.getName() + ": §f" + event.getMessage();
                // Broadcast to clan members
                for (String memberUUID : plugin.getKlanManager().getKlanMembers(klanName)) {
                    Player p = org.bukkit.Bukkit.getPlayer(java.util.UUID.fromString(memberUUID));
                    if (p != null && p.isOnline()) {
                        p.sendMessage(msg);
                    }
                }
                return;
            }

            String format = event.getFormat();
            // Prefix'i başa ekle: [KlanAdı] <Oyuncu> Mesaj
            event.setFormat("§8[§6" + klanName + "§8] §r" + format);
        }
    }
}
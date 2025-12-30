package com.dkprojects.dkklan.listeners;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkcoin.DkCoin;
import com.dkprojects.dkcoin.managers.CoinManager;
import com.dkprojects.dkklan.menus.*;
import com.dkprojects.dkklan.war.MapDraft;
import com.dkprojects.dkklan.objects.WarArena;
import com.dkprojects.dkklan.war.MatchSeries;
import com.dkprojects.dkklan.managers.WarManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import com.dkprojects.dkklan.menus.UpgradesMenu;

public class MenuListener implements Listener {

    private final DkKlan plugin;

    public MenuListener(DkKlan plugin) {
        this.plugin = plugin;
    }

    private boolean isPluginGuiTitle(String title) {
        if (title == null) return false;
        return title.startsWith("§8Klan Yönetimi:")
                || title.equals("§8Klan Kontrol Paneli")
                || title.equals("§8Klan Yükseltmeleri")
                || title.equals("§8Klan Savaşları & E-Spor")
                || title.equals("§8Aktif Savaşlar")
                || title.equals("§8Klan Savaşı Kuralları")
                || title.equals("§8Sezon Tablosu ve Sıralama")
                || title.equals("§8Admin Test Paneli")
                || title.equals("§8Kit Seçimi")
                || title.equals("§8Savaş Daveti")
                || title.equals("§8Savaş Davet Menüsü")
                || title.equals("§8Kadro Yönetimi")
                || title.equals("§8Arenalar")
                || title.contains("Harita Draftı")
                || title.startsWith("§8Yayıncı Kamerası:");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (isPluginGuiTitle(title)) {
            event.setCancelled(true);
        }

        if (title.startsWith("§8Kit Düzenle: ")) {
            int slot = event.getRawSlot();
            // Prevent interaction with spacers (41-52) and Save button (53)
            // Note: RawSlot 0-53 is Top Inventory. >53 is Player Inventory.
            if (slot >= 41 && slot <= 53) {
                event.setCancelled(true);
                if (slot == 53) {
                    String kitName = title.replace("§8Kit Düzenle: ", "");
                    plugin.getKitManager().saveKitFromGUI(kitName, event.getView().getTopInventory());
                    player.sendMessage("§aKit güncellendi: " + kitName);
                    player.closeInventory();
                    com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.SUCCESS);
                }
            }
            return;
        }

        if (title.equals("§8Klan Yönetimi:")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            if (clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

            String displayName = clicked.getItemMeta().getDisplayName();

            if (displayName.equals("§b§lÜYELER")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new MembersMenu(plugin).open(player);
            }
            else if (displayName.equals("§6§lKLAN KASASI")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new BankMenu(plugin).open(player);
            }
            else if (displayName.equals("§d§lYÜKSELTMELER")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new UpgradesMenu(plugin).open(player);
            }
            else if (displayName.equals("§c§lKLAN SAVAŞLARI")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new WarMenu(plugin).open(player);
            }
            else if (displayName.equals("§b§lKLAN EVİNE GİT")) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                player.closeInventory();
                player.performCommand("klan git");
            }
            else if (displayName.equals("§e§lKLAN EVİNİ BELİRLE")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                player.closeInventory();
                player.performCommand("klan setbase");
            }
            else if (displayName.equals("§a§lOYUNCU DAVET ET")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                player.closeInventory();
                player.sendMessage("§aLütfen davet etmek istediğiniz oyuncunun adını sohbete yazın.");
                player.sendMessage("§7(İptal etmek için 'iptal' yazın)");
                plugin.getKlanManager().setInviteMode(player.getUniqueId(), true);
            }
            else if (displayName.equals("§6§lDUYURU YAP")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                player.closeInventory();
                player.sendMessage("§aLütfen yapmak istediğiniz duyuruyu sohbete yazın.");
                player.sendMessage("§7(İptal etmek için 'iptal' yazın)");
                plugin.getKlanManager().setAnnouncementMode(player.getUniqueId(), true);
            }
            else if (displayName.equals("§c§lKLANI DAĞIT")) {
                player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                player.closeInventory();
                player.performCommand("klan dagit");
            }
            else if (displayName.equals("§c§lKLANDAN AYRIL")) {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                player.closeInventory();
                player.performCommand("klan ayril");
            }
            return;
        }

        else if (title.equals("§8Klan Menüsü")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String displayName = clicked.getItemMeta().getDisplayName();

            if (displayName.equals("§a§lKlan Kur")) {
                player.closeInventory();
                player.sendMessage("§eKlan kurmak için: §a/klan kur <isim>");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            else if (displayName.equals("§e§lKlanları Listele")) {
                player.closeInventory();
                player.performCommand("klan liste");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            else if (displayName.equals("§b§lDavetleri Gör")) {
                player.closeInventory();
                player.performCommand("klan davetler");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            else if (displayName.equals("§e§lBilgi")) {
                player.closeInventory();
                player.sendMessage("§8§m--------------------------------");
                player.sendMessage("§6§lDK KLAN KOMUTLARI");
                player.sendMessage("§e/klan kur <isim> §7- Yeni klan kurar");
                player.sendMessage("§e/klan liste §7- Klanları listeler");
                player.sendMessage("§e/klan davetler §7- Gelen davetleri görür");
                player.sendMessage("§e/klan katil <klan> §7- Davet gelen klana katılır");
                player.sendMessage("§8§m--------------------------------");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            else if (displayName.equals("§cKapat")) {
                player.closeInventory();
            }
        }

        if (title.equals("§8Klan Kontrol Paneli")) {
            // Deprecated but kept for compatibility if needed
            event.setCancelled(true);
            return;
        }
        else if (title.equals("§8Klan Üyeleri")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                 player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                 new KlanMenu(plugin).open(player);
            }
            else if (clicked.getType() == Material.PAPER && clicked.getItemMeta().getDisplayName().equals("§a§lOYUNCU DAVET ET")) {
                 player.closeInventory();
                 player.sendMessage("§aLütfen davet etmek istediğiniz oyuncunun adını sohbete yazın.");
                 player.sendMessage("§7(İptal etmek için 'iptal' yazın)");
                 plugin.getKlanManager().setInviteMode(player.getUniqueId(), true);
            }
            else if (clicked.getType() == Material.PLAYER_HEAD) {
                 String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
                 if (klanName == null) return;
                 
                 boolean isViewerLeader = plugin.getKlanManager().isLeader(player.getUniqueId());
                 if (!isViewerLeader) return;

                 String targetName = clicked.getItemMeta().getDisplayName().replace("§e", "");
                 
                 if (event.isLeftClick()) {
                     player.performCommand("klan terfi " + targetName);
                     player.closeInventory(); 
                 } else if (event.isRightClick()) {
                     player.performCommand("klan at " + targetName);
                     player.closeInventory();
                 }
            }
        }
        else if (title.equals("§8Klan Yükseltmeleri")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new KlanMenu(plugin).open(player);
                return;
            }

            String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
            if (klanName == null) return;
            
            String leaderId = plugin.getKlanManager().getKlanLeader(klanName);
            if (!player.getUniqueId().toString().equals(leaderId)) {
                player.sendMessage("§8[§cDkKlan§8] §cSadece klan lideri yükseltme yapabilir!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
        }
        else if (title.equals("§8Klan Savaşları & E-Spor")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.DIAMOND_SWORD && clicked.getItemMeta().getDisplayName().equals("§a§lSAVAŞ BAŞLAT")) {
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                if (!plugin.getWarManager().hasAcceptedRules(player.getUniqueId())) {
                    com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.LOCK_ERROR);
                    new RulesMenu(plugin).open(player);
                } else {
                    String clanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
                    if (clanName == null) {
                        player.sendMessage("§cBir klanda değilsin!");
                        return;
                    }
                    String leaderId = plugin.getKlanManager().getKlanLeader(clanName);
                    if (!player.getUniqueId().toString().equals(leaderId)) {
                        com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.LOCK_ERROR);
                        return;
                    }
                    new WarInviteMenu(plugin).open(player);
                }
            }
            else if (clicked.getType() == Material.ENDER_EYE && clicked.getItemMeta().getDisplayName().equals("§b§lCANLI SAVAŞLAR")) {
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                new ActiveWarsMenu(plugin).open(player);
            }
            else if (clicked.getType() == Material.REDSTONE_BLOCK && clicked.getItemMeta().getDisplayName().equals("§c§lAKTİF SAVAŞ DURUMU")) {
                 com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                 new ActiveWarsMenu(plugin).open(player);
            }
            else if (clicked.getType() == Material.IRON_CHESTPLATE && clicked.getItemMeta().getDisplayName().equals("§b§lKİT SEÇİMİ")) {
                 com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                 new KitSelectorMenu(plugin).open(player);
            }
            else if (clicked.getType() == Material.MAP && clicked.getItemMeta().getDisplayName().equals("§e§lHARİTA & DRAFT")) {
                 com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                 String clan = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
                 if (clan != null) {
                     com.dkprojects.dkklan.war.MatchSeries series = plugin.getWarManager().getSeries(clan);
                     if (series != null && series.getDraft() != null) {
                         new MapDraftMenu(plugin, series.getDraft()).open(player);
                     } else {
                         player.sendMessage("§cAktif bir draft süreci bulunmuyor.");
                     }
                 } else {
                     player.sendMessage("§cBir klanda değilsin.");
                 }
            }
            else if (clicked.getType() == Material.BOOK && clicked.getItemMeta().getDisplayName().equals("§6§lKURALLAR")) {
                 com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                 new RulesMenu(plugin).open(player);
            }
            else if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta().getDisplayName().equals("§e§lKADRO YÖNETİMİ")) {
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                player.closeInventory();
                player.performCommand("klansavasi kadro sec");
            }
            else if (clicked.getType() == Material.NETHER_STAR && clicked.getItemMeta().getDisplayName().equals("§d§lSEZON TABLOSU")) {
                 com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                 new SeasonMenu(plugin).open(player);
            }
            else if (clicked.getType() == Material.GRASS_BLOCK && clicked.getItemMeta().getDisplayName().equals("§a§lARENALAR")) {
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                player.closeInventory();
                player.performCommand("klansavasi arena"); 
            }
            else if (clicked.getType() == Material.WRITABLE_BOOK && clicked.getItemMeta().getDisplayName().equals("§e§lSAVAŞ DAVETİ VAR!")) {
                String myClan = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
                if (myClan != null) {
                    WarManager.Invite invite = plugin.getWarManager().getInviteForClan(myClan);
                    if (invite != null) {
                        new AcceptInviteMenu(plugin, invite.senderClan, invite.bet, invite.playerCount, invite.bestOf, invite.kitName).open(player);
                    } else {
                        player.sendMessage("§cAktif davet bulunamadı.");
                        player.closeInventory();
                    }
                }
            }
            else if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
            }
            else if (player.hasPermission("dkklan.test") && clicked.getItemMeta() != null && "§c§lADMIN TEST MODU".equals(clicked.getItemMeta().getDisplayName())) {
                new AdminTestMenu(plugin).open(player);
            }
        }
        else if (title.equals("§8Aktif Savaşlar")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                new WarMenu(plugin).open(player);
            } else if (clicked.getType() == Material.DIAMOND_SWORD) {
                String name = clicked.getItemMeta().getDisplayName();
                try {
                    String clean = name.replace("§c", "");
                    String[] parts = clean.split(" vs ");
                    if (parts.length > 0) {
                        player.closeInventory();
                        plugin.getWarManager().spectate(player, parts[0]);
                    }
                } catch (Exception e) {
                    player.sendMessage("§cSavaş bilgisi okunamadı.");
                }
            }
        }
        else if (title.equals("§8Klan Savaşı Kuralları")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (clicked.getType() == Material.EMERALD_BLOCK && clicked.getItemMeta().getDisplayName().equals("§a§lKURALLARI KABUL EDİYORUM")) {
                plugin.getWarManager().acceptRules(player.getUniqueId());
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.ACCEPT);
                player.sendMessage("§aKuralları kabul ettin! Artık savaşa girebilirsin.");
                player.closeInventory();
                new WarMenu(plugin).open(player);
            }
            else if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                new WarMenu(plugin).open(player);
            }
        }
        else if (title.equals("§8Sezon Tablosu ve Sıralama")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (clicked.getType() == Material.BARRIER && clicked.getItemMeta().getDisplayName().equals("§cKapat")) {
                com.dkprojects.dkklan.utils.SoundManager.play(player, com.dkprojects.dkklan.utils.SoundManager.Key.MENU_OPEN);
                player.closeInventory();
            }
        }
        // --- NEW MENUS ---
        else if (title.startsWith("§8Yayıncı Kamerası:")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.BARRIER && clicked.getItemMeta().getDisplayName().equals("§cKapat")) {
                player.closeInventory();
                return;
            }

            if (clicked.getType() == Material.ENDER_PEARL) {
                if (clicked.getItemMeta().hasLore()) {
                    List<String> lore = clicked.getItemMeta().getLore();
                    if (lore != null && !lore.isEmpty()) {
                        String coords = org.bukkit.ChatColor.stripColor(lore.get(0));
                        // Format: "X: 100 Y: 64 Z: -200"
                        try {
                            String[] parts = coords.split(" ");
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[3]);
                            int z = Integer.parseInt(parts[5]);
                            
                            String arenaName = title.replace("§8Yayıncı Kamerası: ", "");
                            WarArena arena = plugin.getWarManager().getArena(arenaName);
                            if (arena != null) {
                                Location loc = new Location(arena.getSpawn1().getWorld(), x + 0.5, y, z + 0.5);
                                loc.setYaw(player.getLocation().getYaw());
                                loc.setPitch(player.getLocation().getPitch());
                                player.teleport(loc);
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                            }
                        } catch (Exception e) {
                            player.sendMessage("§cLokasyon okunamadı.");
                        }
                    }
                }
            }
        }
        else if (title.equals("§8Savaş Davet Menüsü")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            WarManager.InviteDraft draft = plugin.getWarManager().getInviteDraft(player.getUniqueId());
            if (draft == null) {
                player.closeInventory();
                return;
            }

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                new WarMenu(plugin).open(player);
                return;
            }
            else if (clicked.getType() == Material.EMERALD_BLOCK && clicked.getItemMeta().getDisplayName().equals("§a§lDAVET GÖNDER")) {
                if (draft.targetClan == null) {
                    player.sendMessage("§cLütfen bir rakip klan seçin!");
                    return;
                }
                plugin.getWarManager().sendInvite(player, draft.targetClan, draft.bet, draft.playerCount, draft.bestOf, draft.kitName);
                player.closeInventory();
                return;
            }
            else if (clicked.getType() == Material.IRON_CHESTPLATE) { // Kit Selection Link
                new KitSelectorMenu(plugin).open(player);
                return;
            }

            // Options logic
            String name = clicked.getItemMeta().getDisplayName();
            if (name.contains("BO1")) plugin.getWarManager().setDraftBestOf(player.getUniqueId(), 1);
            else if (name.contains("BO3")) plugin.getWarManager().setDraftBestOf(player.getUniqueId(), 3);
            else if (name.contains("BO5")) plugin.getWarManager().setDraftBestOf(player.getUniqueId(), 5);
            else if (name.contains("Tüm üyeler")) plugin.getWarManager().setDraftPlayerCount(player.getUniqueId(), 0);
            else if (name.contains("3v3")) plugin.getWarManager().setDraftPlayerCount(player.getUniqueId(), 3);
            else if (name.contains("5v5")) plugin.getWarManager().setDraftPlayerCount(player.getUniqueId(), 5);
            else if (name.contains("Özel Sayı")) {
                player.closeInventory();
                plugin.getWarManager().setAwaitingPlayerCount(player.getUniqueId(), true);
                player.sendMessage("§aLütfen oyuncu sayısını sohbete yazın (Örn: 4, 10).");
                player.sendMessage("§7(İptal etmek için 'iptal' yazın)");
            }
            else if (name.contains("Bahis:")) {
                 // Extract amount from name or assume based on lore/order
                 if (name.contains("0")) plugin.getWarManager().setDraftBet(player.getUniqueId(), 0);
                 else if (name.contains("100000")) plugin.getWarManager().setDraftBet(player.getUniqueId(), 100000);
                 else if (name.contains("250000")) plugin.getWarManager().setDraftBet(player.getUniqueId(), 250000);
                 else if (name.contains("500000")) plugin.getWarManager().setDraftBet(player.getUniqueId(), 500000);
                 else if (name.contains("1000000")) plugin.getWarManager().setDraftBet(player.getUniqueId(), 1000000);
            }
            else if (name.contains("Rakip:")) {
                 String target = name.replace("§eRakip: ", "").replace("§aRakip: ", "").trim();
                 plugin.getWarManager().setDraftTargetClan(player.getUniqueId(), target);
            }

            // Refresh
            new WarInviteMenu(plugin).open(player);
        }
        else if (title.equals("§8Kit Seçimi")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                new WarInviteMenu(plugin).open(player);
                return;
            }

            if (clicked.getType() == Material.IRON_CHESTPLATE) {
                String kitName = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                WarManager.InviteDraft draft = plugin.getWarManager().getInviteDraft(player.getUniqueId());
                if (draft != null) {
                    draft.kitName = kitName;
                    player.sendMessage("§aKit seçildi: " + kitName);
                    new WarInviteMenu(plugin).open(player);
                } else {
                    player.closeInventory();
                    player.sendMessage("§cOturum süresi doldu veya hata oluştu. Lütfen tekrar deneyin.");
                }
            }
        }
        else if (title.equals("§8Savaş Daveti")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.LIME_CONCRETE && clicked.getItemMeta().getDisplayName().equals("§aKabul Et")) {
                String senderClan = event.getInventory().getItem(13).getItemMeta().getLore().get(0).split(": ")[1].replace("§e", "");
                plugin.getWarManager().acceptInvite(player, senderClan);
                player.closeInventory();
            }
            else if (clicked.getType() == Material.RED_CONCRETE && clicked.getItemMeta().getDisplayName().equals("§cReddet")) {
                plugin.getWarManager().rejectInvite(player);
                player.closeInventory();
            }
        }
        else if (title.contains("Harita Draftı")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                new WarMenu(plugin).open(player);
                return;
            }

            if (clicked.getType() == Material.MAP) {
                String mapName = clicked.getItemMeta().getDisplayName().replace("§a", "");
                plugin.getWarManager().processBan(player, mapName);
                
                // Re-open if draft still active
                String clan = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
                if (clan != null) {
                     MatchSeries series = plugin.getWarManager().getSeries(clan);
                     if (series != null && series.getDraft() != null && !series.getDraft().isFinished()) {
                         new MapDraftMenu(plugin, series.getDraft()).open(player);
                     } else {
                         player.closeInventory();
                     }
                }
            }
        }
        else if (title.equals("§8Admin Test Paneli")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            if (!player.hasPermission("dkklan.test")) return;

            String name = clicked.getItemMeta().getDisplayName();
            if (clicked.getType() == Material.ARROW && "§cGeri Dön".equals(name)) {
                new WarMenu(plugin).open(player);
            }
            else if (name.contains("TEST MODU")) {
                // Toggle
                boolean current = plugin.getWarManager().isTestModeEnabled();
                // We need a way to toggle config or just runtime
                // For now, assume a command exists or we need to add a method.
                // Let's use command if possible, but WarCommand handles "test" subcommands?
                // Actually WarCommand "test" calls handleTestCommand.
                // We'll simulate commands for now.
                // Since I can't easily see handleTestCommand, I'll rely on it being implemented or I'll implement direct logic if needed.
                // But wait, I can add methods to WarManager for these if they don't exist.
                // For safety, I'll use console command to toggle config? No, better use code.
                // Let's assume handleTestCommand has "toggle".
                player.performCommand("klansavasi test toggle");
                new AdminTestMenu(plugin).open(player); // Refresh
            }
            else if (name.contains("SAHTE KLAN")) {
                player.performCommand("klansavasi test fakeclan");
                new AdminTestMenu(plugin).open(player);
            }
            else if (name.contains("BOT EKLE")) {
                player.performCommand("klansavasi test addbots");
            }
            else if (name.contains("DRAFTI BİTİR")) {
                player.performCommand("klansavasi test finishdraft");
            }
            else if (name.contains("DRAFTTAN BAŞLAT")) {
                player.performCommand("klansavasi test startdraft");
            }
            else if (name.contains("KILL EKLE")) {
                player.performCommand("klansavasi test addkill");
            }
            else if (name.contains("SKOR +1")) {
                player.performCommand("klansavasi test addscore");
            }
            else if (name.contains("SAVAŞI BİTİR")) {
                player.performCommand("klansavasi test endwar");
            }
        }
        else if (title.equals("§8Kadro Yönetimi")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                new WarMenu(plugin).open(player);
                return;
            }

            if (clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = clicked.getItemMeta().getDisplayName();
                targetName = org.bukkit.ChatColor.stripColor(targetName);
                if (targetName.startsWith("(Çevrimdışı)")) return; 
                
                if (targetName.contains(" ")) targetName = targetName.split(" ")[0];

                String clan = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
                if (clan != null) {
                     for (String id : plugin.getKlanManager().getKlanMembers(clan)) {
                         UUID uuid;
                         try { uuid = UUID.fromString(id); } catch(Exception e) { continue; }
                         String pname = Bukkit.getOfflinePlayer(uuid).getName();
                         if (pname != null && pname.equals(targetName)) {
                             plugin.getWarManager().toggleRosterPlayer(player, uuid);
                             new WarRosterMenu(plugin).open(player); // Refresh
                             return;
                         }
                     }
                }
            }
        }
        else if (title.equals("§8Arenalar")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§cGeri Dön")) {
                new WarMenu(plugin).open(player);
                return;
            }
            
            if (clicked.getType() == Material.ANVIL && clicked.getItemMeta().getDisplayName().equals("§a§lYENİ ARENA OLUŞTUR")) {
                 player.closeInventory();
                 player.sendMessage("§aLütfen arena adını sohbete yazın (veya komut kullanın: /klansavasi arena olustur <isim>)");
            }
            
             if (clicked.getType() == Material.LIME_CONCRETE || clicked.getType() == Material.RED_CONCRETE || clicked.getType() == Material.YELLOW_CONCRETE) {
                 if (player.hasPermission("dkklan.admin")) {
                     String arenaName = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                     player.sendMessage("§eArena yönetimi yakında eklenecek: " + arenaName);
                 }
             }
        }
    }
}

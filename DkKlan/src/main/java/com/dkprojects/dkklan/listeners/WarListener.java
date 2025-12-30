package com.dkprojects.dkklan.listeners;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.war.War;
import com.dkprojects.dkklan.war.WarState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class WarListener implements Listener {
    private final DkKlan plugin;

    public WarListener(DkKlan plugin) {
        this.plugin = plugin;
    }

    private War getWar(Player p) {
        String klanName = plugin.getKlanManager().getPlayerKlan(p.getUniqueId());
        if (klanName == null) return null;
        return plugin.getWarManager().getWar(klanName);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        War war = getWar(p);
        
        if (war != null) {
            if (war.getState() == WarState.PREPARING) {
                // Lobby logic
            } else if (war.getState() == WarState.STARTED) {
                // Arena Boundary Check
                if (war.getParticipants().contains(p.getUniqueId())) {
                     if (!war.getArena().isInside(e.getTo())) {
                         e.setCancelled(true);
                         p.sendMessage("§cArena dışına çıkamazsın!");
                     }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        War war = getWar(p);
        
        if (war != null && war.getState() == WarState.PREPARING) {
            e.setCancelled(true); // No damage in lobby
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        Player attacker = (Player) e.getDamager();
        
        // Spectator Check
        if (plugin.getWarManager().isSpectating(attacker)) {
            e.setCancelled(true);
            return;
        }

        String victimClan = plugin.getKlanManager().getPlayerKlan(victim.getUniqueId());
        String attackerClan = plugin.getKlanManager().getPlayerKlan(attacker.getUniqueId());

        if (victimClan == null || attackerClan == null) return;

        War war = plugin.getWarManager().getWar(attackerClan);
        
        if (war != null) {
            if (war.getState() == WarState.PREPARING) {
                e.setCancelled(true);
                attacker.sendMessage("§cSavaş başlamadan vuramazsın!");
                return;
            }
            
            if (war.isActive()) {
                if (war.getClanA().equals(victimClan) || war.getClanB().equals(victimClan)) {
                    if (victimClan.equals(attackerClan) && !war.getRuleset().isFriendlyFire()) {
                        e.setCancelled(true);
                        attacker.sendMessage(plugin.getMessage("friendly-fire-deny"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        War war = getWar(p);
        
        if (war != null && war.isActive() && war.getParticipants().contains(p.getUniqueId())) {
            if (e.getBlock().getType() == Material.COBWEB) {
                if (!war.getRuleset().isAllowCobweb()) {
                    e.setCancelled(true);
                    p.sendMessage("§cBu savaşta Cobweb yasak!");
                    return;
                }
                // Allow Cobweb, schedule remove
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (e.getBlock().getType() == Material.COBWEB) {
                        e.getBlock().setType(Material.AIR);
                    }
                }, 100L); // 5 seconds
            } else {
                e.setCancelled(true);
                p.sendMessage("§cSavaşta sadece Cobweb koyabilirsin!");
            }
        } else if (plugin.getWarManager().isSpectating(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        War war = getWar(p);
        
        if (war != null && war.isActive() && war.getParticipants().contains(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage("§cSavaşta blok kıramazsın!");
        } else if (plugin.getWarManager().isSpectating(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) return; // OPs bypass
        
        War war = getWar(p);
        if (war != null && (war.isActive() || war.getState() == WarState.PREPARING)) {
            if (war.getParticipants().contains(p.getUniqueId())) {
                String cmd = e.getMessage().toLowerCase();
                if (cmd.startsWith("/klansavasi") || cmd.startsWith("/msg") || cmd.startsWith("/r")) return; // Allow war commands and chat
                e.setCancelled(true);
                p.sendMessage("§cSavaş sırasında komut kullanamazsın!");
            }
        }
    }
    
    @EventHandler
    public void onElytra(org.bukkit.event.entity.EntityToggleGlideEvent e) {
         if (!(e.getEntity() instanceof Player)) return;
         Player p = (Player) e.getEntity();
         if (p.isOp()) return;
         
         War war = getWar(p);
         if (war != null && (war.isActive() || war.getState() == WarState.PREPARING)) {
             if (war.getParticipants().contains(p.getUniqueId())) {
                 if (e.isGliding() && !war.getRuleset().isAllowElytra()) {
                     e.setCancelled(true);
                     p.sendMessage("§cSavaşta Elytra kullanamazsın!");
                 }
             }
         }
    }
    
    @EventHandler
    public void onFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) return;
        
        War war = getWar(p);
        if (war != null && (war.isActive() || war.getState() == WarState.PREPARING)) {
            if (war.getParticipants().contains(p.getUniqueId())) {
                if (e.isFlying()) {
                    e.setCancelled(true);
                    p.setFlying(false);
                    p.setAllowFlight(false);
                    p.sendMessage("§cSavaşta uçamazsın!");
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player p = e.getPlayer();
            War war = getWar(p);
            
            if (war != null && war.isActive() && war.getParticipants().contains(p.getUniqueId())) {
                if (e.getItem() != null) {
                    if (e.getItem().getType() == Material.ENDER_PEARL && !war.getRuleset().isAllowEnderPearl()) {
                        e.setCancelled(true);
                        p.sendMessage("§cSavaşta Ender Pearl kullanamazsın!");
                        return;
                    }
                    if (war.getRuleset().getBlacklistedItems().contains(e.getItem().getType().name())) {
                        e.setCancelled(true);
                        p.sendMessage("§cBu eşya yasaklandı: " + e.getItem().getType().name());
                        return;
                    }
                }
                
                // Block container access if inventory lock is on
                if (e.getClickedBlock() != null && war.getRuleset().isInventoryLock()) {
                    Material type = e.getClickedBlock().getType();
                    if (type == Material.ENDER_CHEST || type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL ||
                        type == Material.CRAFTING_TABLE || type == Material.FURNACE || type == Material.BLAST_FURNACE || type == Material.SMOKER ||
                        type == Material.BREWING_STAND || type == Material.HOPPER || type == Material.DROPPER || type == Material.DISPENSER ||
                        type == Material.BARREL || type == Material.SHULKER_BOX) {
                        e.setCancelled(true);
                        p.sendMessage("§cSavaşta bu bloğu kullanamazsın!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        String victimClan = plugin.getKlanManager().getPlayerKlan(victim.getUniqueId());
        
        if (victimClan == null) return;
        
        War war = plugin.getWarManager().getWar(victimClan);
        if (war != null && war.isActive()) {
            if (war.getRuleset().isKeepInventory()) {
                e.setKeepInventory(true);
                e.getDrops().clear();
            }

            war.addDeath(victim.getUniqueId(), victimClan);

            // Force Respawn to skip death screen
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline() && victim.isDead()) {
                    victim.spigot().respawn();
                }
            }, 1L);

            // Set to Spectator for elimination
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline()) {
                    victim.setGameMode(org.bukkit.GameMode.SPECTATOR);
                    victim.sendMessage("§cElendiniz! Savaş bitene kadar izleyici modundasınız.");
                    victim.sendTitle("§cELENDİNİZ!", "§7Takım arkadaşlarınızı izleyebilirsiniz.", 10, 60, 20);
                    // Ensure full health/food just in case
                    victim.setHealth(20);
                    victim.setFoodLevel(20);
                }
                war.checkElimination();
            }, 2L); 

            if (victim.getKiller() != null) {
                Player killer = victim.getKiller();
                String killerClan = plugin.getKlanManager().getPlayerKlan(killer.getUniqueId());
                if (killerClan != null) {
                    war.addKill(killer.getUniqueId(), killerClan);
                }
            }
        }
    }

    @EventHandler
    public void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.POTION) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                e.getPlayer().getInventory().remove(Material.GLASS_BOTTLE);
            }, 1L);
        }
    }

    @EventHandler
    public void onLiquidFlow(org.bukkit.event.block.BlockFromToEvent e) {
        // Prevent liquid spread in war arenas
        if (e.getBlock().getType() == Material.WATER || e.getBlock().getType() == Material.LAVA) {
            // We should check if this block is in an active war arena to avoid affecting the whole world
            // But for safety and performance, we can check if any war is active in this world or if the location is inside an arena
            // For now, let's assume if it's in a war world/arena context.
            // A simple check is: Is there an active war? 
            // Better: Check if location is inside ANY active war arena.
            // Since iterating all wars might be heavy for every fluid tick, we can optimize.
            // However, the user request implies a general rule for the war logic.
            // Let's check if the world matches a war world.
            
            // Optimization: If the plugin is dedicated to wars or this is the war world.
            // Let's check if the block is within an active arena.
            boolean insideArena = false;
            for (War war : plugin.getWarManager().getActiveWars()) {
                if (war.getArena().getPos1().getWorld().equals(e.getBlock().getWorld())) {
                    if (war.getArena().isInside(e.getBlock().getLocation())) {
                        insideArena = true;
                        break;
                    }
                }
            }
            
            if (insideArena) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        String klanName = plugin.getKlanManager().getPlayerKlan(p.getUniqueId());
        if (klanName == null) return;
        
        War war = plugin.getWarManager().getWar(klanName);
        if (war != null) {
            if (war.getParticipants().contains(p.getUniqueId())) {
                 // Check continuity after 1 tick to ensure player is seen as offline
                 plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                     plugin.getWarManager().checkWarContinuity(war);
                 }, 1L);
                 
                 // Restore inventory if kit mode was active
                 if (war.getRuleset().isKitMode()) {
                     p.getInventory().clear();
                     war.restoreOriginalInventory(p);
                 }
                 
                 // If war started, remove player permanently
                 if (war.isActive()) {
                     war.removePlayer(p.getUniqueId(), klanName);
                     // Notify
                     // We can't broadcast to this war easily from here without helper, but WarManager handles notifications on continuity check usually.
                     // But checkWarContinuity uses getOnlineCount, which looks at list.
                     // If we remove player, getOnlineCount will decrease.
                     // checkWarContinuity will handle "Tüm takım çıkarsa".
                 }
            }
        }
        
        if (plugin.getWarManager().isSpectating(p)) {
            plugin.getWarManager().stopSpectating(p);
        }
    }
    
    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        War war = getWar(p);
        
        if (war != null && (war.isActive() || war.getState() == WarState.PREPARING)) {
            if (war.getParticipants().contains(p.getUniqueId()) && war.getRuleset().isInventoryLock()) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        War war = getWar(p);
        
        if (war != null && (war.isActive() || war.getState() == WarState.PREPARING)) {
            // Allow inventory rearrangement even if inventory lock is on
            // Only block if it interacts with other inventories (which is handled by onInteract usually, 
            // but here we might want to ensure they don't take items OUT if using a container)
            // For now, removing the lock check to allow kit rearrangement as requested.
            /*
            if (war.getParticipants().contains(p.getUniqueId()) && war.getRuleset().isInventoryLock()) {
                e.setCancelled(true);
            }
            */
        }
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        War war = getWar(p);
        
        if (war != null && (war.isActive() || war.getState() == WarState.PREPARING)) {
            if (war.getParticipants().contains(p.getUniqueId()) && war.getRuleset().isInventoryLock()) {
                e.setCancelled(true);
            }
        }
    }
}

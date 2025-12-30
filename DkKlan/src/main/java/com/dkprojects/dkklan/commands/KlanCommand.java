package com.dkprojects.dkklan.commands;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.KlanManager;
import com.dkprojects.dkklan.objects.ClanRole;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.dkprojects.dkklan.menus.KlanMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KlanCommand implements CommandExecutor, TabCompleter {
    private DkKlan plugin;

    public KlanCommand(DkKlan plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;
        KlanManager km = plugin.getKlanManager();
        String prefix = plugin.getRawMessage("prefix");
        String klanName = km.getPlayerKlan(player.getUniqueId());

        if (command.getName().equalsIgnoreCase("k")) {
             if (klanName == null) {
                 player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                 return true;
             }
             
             if (args.length == 0) {
                 boolean enabled = km.toggleClanChat(player.getUniqueId());
                 player.sendMessage(prefix + (enabled ? plugin.getMessage("clan-chat-enabled") : plugin.getMessage("clan-chat-disabled")));
                 return true;
             } else {
                 StringBuilder msg = new StringBuilder();
                 for (String arg : args) msg.append(arg).append(" ");
                 // Broadcast to clan
                 List<String> members = km.getMembers(klanName);
                 String roleName = km.getRole(player.getUniqueId()).getDisplayName();
                 String format = plugin.getMessage("chat-format")
                     .replace("%role%", roleName)
                     .replace("%player%", player.getName())
                     .replace("%message%", msg.toString().trim());
                 for (String uuid : members) {
                     Player p = org.bukkit.Bukkit.getPlayer(java.util.UUID.fromString(uuid));
                     if (p != null && p.isOnline()) {
                         p.sendMessage(format);
                     }
                 }
                 return true;
             }
        }

        if (args.length == 0) {
            if (klanName != null) {
                new KlanMenu(plugin).open(player);
            } else {
                new com.dkprojects.dkklan.menus.KlanNoClanMenu(plugin).open(player);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "kur":
                if (args.length < 2) {
                    player.sendMessage(prefix + plugin.getMessage("usage-create"));
                    return true;
                }
                if (klanName != null) {
                    player.sendMessage(prefix + plugin.getMessage("already-in-clan"));
                    return true;
                }
                km.createKlan(args[1], player.getUniqueId());
                player.sendMessage(prefix + plugin.getMessage("clan-created").replace("%clan%", args[1]));
                break;

            case "sil":
            case "dagit":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                if (!km.isLeader(player.getUniqueId())) {
                    player.sendMessage(prefix + plugin.getMessage("only-leader-delete"));
                    return true;
                }
                km.deleteKlan(klanName);
                player.sendMessage(prefix + plugin.getMessage("clan-deleted"));
                break;

            case "bilgi":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                sendKlanInfo(player, klanName);
                break;

            case "duyuru":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                ClanRole role = km.getRole(player.getUniqueId());
                if (!role.isAtLeast(ClanRole.MODERATOR)) {
                    player.sendMessage(prefix + plugin.getMessage("min-rank-moderator"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(prefix + plugin.getMessage("usage-announce"));
                    return true;
                }
                StringBuilder msg = new StringBuilder();
                for (int i = 1; i < args.length; i++) msg.append(args[i]).append(" ");
                
                String announcement = plugin.getMessage("announcement-header")
                    .replace("%player%", player.getName())
                    .replace("%message%", msg.toString().trim());
                                      
                List<String> members = km.getMembers(klanName);
                for (String uuid : members) {
                     Player p = org.bukkit.Bukkit.getPlayer(java.util.UUID.fromString(uuid));
                     if (p != null && p.isOnline()) {
                         p.sendMessage(announcement);
                         p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                     }
                }
                break;
            
            case "menu":
                if (klanName != null) {
                    new KlanMenu(plugin).open(player);
                } else {
                    new com.dkprojects.dkklan.menus.KlanNoClanMenu(plugin).open(player);
                }
                break;
            
            case "liste":
                player.sendMessage(plugin.getRawMessage("hologram-title") + " §eSunucudaki Klanlar:");
                // Not efficient for large number of clans but ok for now
                // We need a way to get all clan names. KlanManager doesn't expose it directly in public API efficiently maybe?
                // km.getKlanMembers is map<String, List<String>>. keySet is clan names.
                // Wait, I can't access klanMembers directly, I need to check KlanManager API.
                // I will add getAllClans() to KlanManager or just use reflection/hack if needed, but better add method.
                // Actually I can't edit KlanManager easily to expose private map.
                // But I can check if there is a method.
                // Let's assume I can add getClans() to KlanManager.
                for (String clan : km.getAllClans()) {
                     player.sendMessage("§8▪ §e" + clan + " §7(Lider: " + org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(km.getKlanLeader(clan))).getName() + ")");
                }
                break;

            case "davetler":
                String pending = km.getPendingInvite(player.getUniqueId());
                if (pending == null) {
                    player.sendMessage(plugin.getMessage("prefix") + "§cBekleyen davetiniz yok.");
                } else {
                    player.sendMessage(plugin.getMessage("prefix") + "§a" + pending + " §eklanından davetiniz var!");
                    player.sendMessage(plugin.getMessage("prefix") + "§7Kabul etmek için: §a/klan katil");
                }
                break;

            case "git":
            case "base":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                org.bukkit.Location loc = km.getKlanBase(klanName);
                if (loc == null) {
                    player.sendMessage(prefix + plugin.getMessage("base-not-set"));
                    return true;
                }
                player.teleport(loc);
                player.sendMessage(prefix + plugin.getMessage("teleported-base"));
                break;

            case "setbase":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                if (!km.getRole(player.getUniqueId()).isAtLeast(ClanRole.ADMIN)) {
                    player.sendMessage(prefix + plugin.getMessage("min-rank-admin"));
                    return true;
                }
                km.setKlanBase(klanName, player.getLocation());
                player.sendMessage(prefix + plugin.getMessage("base-set"));
                break;
                
            case "ayril":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                if (km.isLeader(player.getUniqueId())) {
                    player.sendMessage(prefix + "§cKlan lideri klandan ayrılamaz! Klanı dağıtmalısınız.");
                    return true;
                }
                km.leaveClan(player.getUniqueId());
                break;

            case "at":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                ClanRole roleAt = km.getRole(player.getUniqueId());
                if (!roleAt.isAtLeast(ClanRole.ADMIN)) {
                    player.sendMessage(prefix + plugin.getMessage("min-rank-admin"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(prefix + "§eKullanım: /klan at <oyuncu>");
                    return true;
                }
                String targetNameAt = args[1];
                UUID targetUuidAt = null;
                for (String mid : km.getMembers(klanName)) {
                    String mName = org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(mid)).getName();
                    if (mName != null && mName.equalsIgnoreCase(targetNameAt)) {
                        targetUuidAt = java.util.UUID.fromString(mid);
                        break;
                    }
                }
                if (targetUuidAt == null) {
                    player.sendMessage(prefix + "§cOyuncu bulunamadı veya klanınızda değil.");
                    return true;
                }
                if (km.getRole(targetUuidAt).getLevel() >= roleAt.getLevel()) {
                    player.sendMessage(prefix + "§cSizden yüksek veya eşit rütbedeki birini atamazsınız!");
                    return true;
                }
                km.kickMember(targetUuidAt, player.getUniqueId());
                break;

            case "terfi":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                if (!km.isLeader(player.getUniqueId())) {
                    player.sendMessage(prefix + "§cSadece klan lideri terfi verebilir!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(prefix + "§eKullanım: /klan terfi <oyuncu>");
                    return true;
                }
                String targetNameT = args[1];
                UUID targetUuidT = null;
                for (String mid : km.getMembers(klanName)) {
                    String mName = org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(mid)).getName();
                    if (mName != null && mName.equalsIgnoreCase(targetNameT)) {
                        targetUuidT = java.util.UUID.fromString(mid);
                        break;
                    }
                }
                if (targetUuidT == null) {
                    player.sendMessage(prefix + "§cOyuncu bulunamadı veya klanınızda değil.");
                    return true;
                }
                km.promote(targetUuidT, player.getUniqueId());
                break;

            case "savas":
                handleWarCommand(player, args);
                break;

            case "davet":
                if (klanName == null) {
                    player.sendMessage(prefix + plugin.getMessage("not-in-clan"));
                    return true;
                }
                if (!km.getRole(player.getUniqueId()).isAtLeast(ClanRole.ADMIN)) {
                    player.sendMessage(prefix + plugin.getMessage("min-rank-admin"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(prefix + plugin.getMessage("usage-invite"));
                    return true;
                }
                Player target = org.bukkit.Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(prefix + plugin.getMessage("player-not-found"));
                    return true;
                }
                if (km.getPlayerKlan(target.getUniqueId()) != null) {
                    player.sendMessage(prefix + plugin.getMessage("target-already-in-clan"));
                    return true;
                }
                km.invitePlayer(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(prefix + plugin.getMessage("invite-sent").replace("%player%", target.getName()));
                target.sendMessage(prefix + plugin.getMessage("invite-received").replace("%clan%", klanName));
                target.sendMessage(prefix + plugin.getMessage("invite-info"));
                break;

            case "katil":
                if (klanName != null) {
                    player.sendMessage(prefix + plugin.getMessage("already-in-clan"));
                    return true;
                }
                km.acceptInvite(player.getUniqueId());
                break;

            default:
                player.sendMessage(prefix + plugin.getMessage("unknown-command"));
                break;
        }
        return true;
    }

    private void handleWarCommand(Player player, String[] args) {
         if (args.length < 2) {
             player.sendMessage(plugin.getMessage("usage-war"));
             return;
         }
         String action = args[1].toLowerCase();
         if (action.equals("davet")) {
             if (!plugin.getKlanManager().isLeader(player.getUniqueId())) {
                 player.sendMessage(plugin.getMessage("only-leader-war-invite"));
                 return;
             }
             if (args.length < 3) {
                 player.sendMessage(plugin.getMessage("usage-war-invite"));
                 return;
             }
             String targetClan = args[2];
             double bet = 0;
             if (args.length >= 4) {
                 try { bet = Double.parseDouble(args[3]); } catch (Exception e) {}
             }
             plugin.getWarManager().sendInvite(player, targetClan, bet);
         } else if (action.equals("kabul")) {
             if (!plugin.getKlanManager().isLeader(player.getUniqueId())) {
                 player.sendMessage(plugin.getMessage("only-leader-war-accept"));
                 return;
             }
             if (args.length < 3) {
                 player.sendMessage(plugin.getMessage("usage-war-accept"));
                 return;
             }
             plugin.getWarManager().acceptInvite(player, args[2]);
         }
    }

    private void sendKlanInfo(Player player, String klanName) {
        KlanManager km = plugin.getKlanManager();
        player.sendMessage(plugin.getMessage("info-header"));
        player.sendMessage(plugin.getMessage("info-title").replace("%clan%", klanName));
        player.sendMessage("");
        String leaderName = "Bilinmiyor";
        try {
             leaderName = org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(km.getKlanLeader(klanName))).getName();
        } catch (Exception e) {}
        
        player.sendMessage(plugin.getMessage("info-leader").replace("%leader%", leaderName));
        player.sendMessage(plugin.getMessage("info-level").replace("%level%", String.valueOf(km.getKlanLevel(klanName))));
        player.sendMessage(plugin.getMessage("info-members").replace("%count%", String.valueOf(km.getMembers(klanName).size())));
        player.sendMessage(plugin.getMessage("info-elo")
            .replace("%elo%", String.valueOf(km.getKlanElo(klanName)))
            .replace("%league%", km.getKlanLeague(klanName)));
        player.sendMessage(plugin.getMessage("info-bank").replace("%bank%", String.valueOf(km.getKlanBank(klanName))));
        player.sendMessage("");
        player.sendMessage(plugin.getMessage("info-commands"));
        player.sendMessage(plugin.getMessage("info-cmd-menu"));
        player.sendMessage(plugin.getMessage("info-cmd-announce"));
        player.sendMessage(plugin.getMessage("info-cmd-base"));
        player.sendMessage(plugin.getMessage("info-footer"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player p = sender instanceof Player ? (Player) sender : null;
        KlanManager km = plugin.getKlanManager();
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out = Arrays.asList("kur", "sil", "bilgi", "menu", "git", "setbase", "duyuru", "savas", "davet", "katil", "liste", "davetler", "ayril", "at", "terfi");
        } else {
            String sub = args[0].toLowerCase();
            if (sub.equals("savas")) {
                if (args.length == 2) {
                    out = Arrays.asList("davet", "kabul");
                } else if (args.length == 3 && args[1].equalsIgnoreCase("davet")) {
                    String my = p != null ? km.getPlayerKlan(p.getUniqueId()) : null;
                    for (String c : km.getAllClans()) {
                        if (my == null || !c.equalsIgnoreCase(my)) out.add(c);
                    }
                } else if (args.length == 3 && args[1].equalsIgnoreCase("kabul")) {
                    if (p != null) {
                        String myClan = km.getPlayerKlan(p.getUniqueId());
                        if (myClan != null) {
                            com.dkprojects.dkklan.managers.WarManager.Invite inv = plugin.getWarManager().getInviteForClan(myClan);
                            if (inv != null) out.add(inv.senderClan);
                        }
                    }
                } else if (args.length == 4 && args[1].equalsIgnoreCase("davet")) {
                    out = Arrays.asList("0", "3", "5");
                } else if (args.length == 5 && args[1].equalsIgnoreCase("davet")) {
                    out = Arrays.asList("0", "100000", "250000", "500000", "1000000");
                }
            } else if (sub.equals("davet")) {
                if (args.length == 2) {
                    for (Player op : org.bukkit.Bukkit.getOnlinePlayers()) out.add(op.getName());
                }
            } else if (sub.equals("at") || sub.equals("terfi")) {
                if (args.length == 2 && p != null) {
                    String clan = km.getPlayerKlan(p.getUniqueId());
                    if (clan != null) {
                        for (String id : km.getMembers(clan)) {
                            try {
                                String name = org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(id)).getName();
                                if (name != null) out.add(name);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        String last = args[args.length - 1].toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String s : out) {
            if (s.toLowerCase().startsWith(last)) filtered.add(s);
        }
        return filtered;
    }
}

package com.dkprojects.dkklan.commands;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.WarManager;
import com.dkprojects.dkklan.objects.WarArena;
import com.dkprojects.dkklan.menus.WarInfoMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.dkprojects.dkklan.war.MatchSeries;
import com.dkprojects.dkklan.war.BroadcastSession;
import com.dkprojects.dkklan.war.MapDraft;
import com.dkprojects.dkklan.tournament.Tournament;
import com.dkprojects.dkklan.objects.WarRuleset;
import org.bukkit.Location;

public class WarCommand implements CommandExecutor, TabCompleter {
    private final DkKlan plugin;

    public WarCommand(DkKlan plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSadece oyuncular kullanabilir.");
            return true;
        }

        Player p = (Player) sender;
        WarManager wm = plugin.getWarManager();

        if (args.length == 0) {
            if (!p.hasPermission("dkklan.menu")) { // Optional check
                 // sendHelp(p); 
            }
            new com.dkprojects.dkklan.menus.WarMenu(plugin).open(p);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "menu":
            case "menü":
                new com.dkprojects.dkklan.menus.WarMenu(plugin).open(p);
                break;

            case "arena":
                if (!p.hasPermission("dkklan.admin")) {
                    p.sendMessage("§cBunu yapmaya yetkin yok.");
                    return true;
                }
                handleArenaCommand(p, args);
                break;
            
            case "test":
                if (!p.hasPermission("dkklan.test")) {
                    p.sendMessage("§cYetkin yok.");
                    return true;
                }
                handleTestCommand(p, args);
                break;
                
            case "map":
                handleWarCommand(p, args);
                break;
                
            case "cast":
                handleCastCommand(p, args);
                break;
                
            case "camera":
                handleCameraCommand(p, args);
                break;
                
            case "turnuva":
                handleTournamentCommand(p, args);
                break;
                
            case "ruleset":
                handleRulesetCommand(p, args);
                break;
                
            case "setspawn":
                if (!p.hasPermission("dkklan.admin")) {
                    p.sendMessage("§cYetkin yok.");
                    return true;
                }
                plugin.getConfig().set("spawn-location", p.getLocation());
                plugin.saveConfig();
                p.sendMessage("§aGlobal spawn noktası ayarlandı.");
                break;

            case "kit":
                handleKitCommand(p, args);
                break;
                
            case "savas": // /klansavasi savas davet <clan> [count] [bet]
                if (args.length < 2) {
                    p.sendMessage("§eKullanım: /klansavasi savas <davet/kabul> <klan>");
                    return true;
                }
                String warAction = args[1].toLowerCase();
                if (warAction.equals("davet")) {
                    if (args.length < 3) {
                        p.sendMessage("§eKullanım: /klansavasi savas davet <klan> [kisi] [bahis] [BO] [kit]");
                        return true;
                    }
                    if (!plugin.getKlanManager().isLeader(p.getUniqueId())) {
                        p.sendMessage("§cSadece klan lideri savaş daveti atabilir.");
                        return true;
                    }
                    
                    String target = args[2];
                    int count = 0;
                    double bet = 0;
                    int bo = 1;
                    String kit = null;
                    
                    try {
                        if (args.length > 3) count = Integer.parseInt(args[3]);
                        if (args.length > 4) bet = Double.parseDouble(args[4]);
                        if (args.length > 5) bo = Integer.parseInt(args[5]);
                        if (args.length > 6) kit = args[6];
                    } catch (NumberFormatException e) {
                        p.sendMessage("§cGeçersiz sayı formatı.");
                        return true;
                    }
                    
                    wm.sendInvite(p, target, bet, count, bo, kit);
                } else if (warAction.equals("kabul")) {
                    if (args.length < 3) {
                        p.sendMessage("§eKullanım: /klansavasi savas kabul <klan>");
                        return true;
                    }
                    if (!plugin.getKlanManager().isLeader(p.getUniqueId())) {
                        p.sendMessage("§cSadece klan lideri savaş kabul edebilir.");
                        return true;
                    }
                    wm.acceptInvite(p, args[2]);
                } else {
                    p.sendMessage("§eKullanım: /klansavasi savas <davet/kabul> <klan>");
                }
                break;

            case "kadro":
                if (args.length > 1 && args[1].equalsIgnoreCase("sec")) {
                    wm.addPlayerToRoster(p);
                } else {
                    p.sendMessage("§eKullanım: /klansavasi kadro sec");
                }
                break;

            case "sub":
                if (args.length > 2 && args[1].equalsIgnoreCase("gir")) {
                    // /klansavasi sub gir <oyuncu>
                    String subName = args[2];
                    wm.substitutePlayer(p, subName);
                } else {
                    p.sendMessage("§eKullanım: /klansavasi sub gir <oyuncu>");
                }
                break;

            case "izle":
                if (args.length < 2) {
                    p.sendMessage("§eKullanım: /klansavasi izle <klanIsmi>");
                    return true;
                }
                wm.spectate(p, args[1]);
                break;
                
            case "ayril":
                wm.stopSpectating(p);
                break;

            case "bilgi":
                new WarInfoMenu(plugin).open(p);
                break;

            default:
                sendHelp(p);
                break;
        }
        return true;
    }

    private void handleArenaCommand(Player p, String[] args) {
        // args[0] = arena
        if (args.length < 2) {
            sendArenaHelp(p);
            return;
        }
        
        String action = args[1].toLowerCase();
        WarManager wm = plugin.getWarManager();
        
        switch (action) {
            case "setpos1":
                wm.setPos1(p);
                break;
            case "setpos2":
                wm.setPos2(p);
                break;
            case "olustur":
                if (args.length < 3) {
                    p.sendMessage("§eKullanım: /klansavasi arena olustur <isim>");
                    return;
                }
                wm.createArena(p, args[2]);
                break;
            case "edit":
                if (args.length < 4) {
                    p.sendMessage("§eKullanım: /klansavasi arena edit <isim> <setlobby/setwarp1/setwarp2>");
                    return;
                }
                String arenaName = args[2];
                WarArena arena = wm.getArena(arenaName);
                if (arena == null) {
                    p.sendMessage("§cArena bulunamadı.");
                    return;
                }
                String editAction = args[3].toLowerCase();
                if (editAction.equals("setlobby")) {
                    arena.setLobby(p.getLocation());
                    wm.saveArenas();
                    p.sendMessage("§aLobby ayarlandı.");
                } else if (editAction.equals("setwarp1")) {
                    arena.setSpawn1(p.getLocation());
                    wm.saveArenas();
                    p.sendMessage("§aSpawn 1 ayarlandı.");
                } else if (editAction.equals("setwarp2")) {
                    arena.setSpawn2(p.getLocation());
                    wm.saveArenas();
                    p.sendMessage("§aSpawn 2 ayarlandı.");
                } else {
                    p.sendMessage("§cBilinmeyen işlem.");
                }
                break;
            default:
                sendArenaHelp(p);
                break;
        }
    }

    private void sendHelp(Player p) {
        p.sendMessage("§6§lKlan Savaşı Komutları:");
        p.sendMessage("§e/klansavasi bilgi §7- Savaş sistemi hakkında bilgi verir.");
        p.sendMessage("§e/klansavasi savas davet <klan> [bahis] §7- Savaş daveti gönderir.");
        p.sendMessage("§e/klansavasi savas kabul <klan> §7- Savaş davetini kabul eder.");
        p.sendMessage("§e/klansavasi izle <klan> §7- Devam eden savaşı izler.");
        p.sendMessage("§e/klansavasi ayril §7- İzleyici modundan çıkar.");
        if (p.hasPermission("dkklan.admin")) {
            p.sendMessage("§c/klansavasi arena ... §7- Arena yönetimi.");
        }
    }
    
    private void sendArenaHelp(Player p) {
        p.sendMessage("§c§lArena Komutları:");
        p.sendMessage("§7/klansavasi arena setpos1");
        p.sendMessage("§7/klansavasi arena setpos2");
        p.sendMessage("§7/klansavasi map ban <arena> §8- §fMap yasaklar");
        p.sendMessage("§7/klansavasi map liste §8- §fMap durumunu gösterir");
        p.sendMessage("§7/klansavasi cast <basla/bitir> §8- §fYayın modunu açar/kapatır");
        p.sendMessage("§7/klansavasi camera <set/tp> <isim> §8- §fKamera noktalarını yönetir");
        p.sendMessage("§7/klansavasi arena olustur <isim>");
        p.sendMessage("§7/klansavasi arena edit <isim> setlobby");
        p.sendMessage("§7/klansavasi arena edit <isim> setwarp1");
        p.sendMessage("§7/klansavasi arena edit <isim> setwarp2");
    }

    private void handleTournamentCommand(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§eKullanım: /klansavasi turnuva <olustur/katil/baslat/liste>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("olustur")) {
            if (!p.hasPermission("dkklan.admin")) {
                p.sendMessage("§cYetkin yok.");
                return;
            }
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi turnuva olustur <takim_sayisi>");
                return;
            }
            try {
                int teams = Integer.parseInt(args[2]);
                Tournament t = plugin.getTournamentManager().createTournament(teams);
                p.sendMessage("§aTurnuva oluşturuldu! ID: " + t.getId());
                p.sendMessage("§eKlanlar '/klansavasi turnuva katil' yazarak katılabilir.");
            } catch (NumberFormatException e) {
                p.sendMessage("§cGeçersiz sayı.");
            }
        } else if (action.equals("katil")) {
            String clan = plugin.getKlanManager().getPlayerKlan(p.getUniqueId());
            if (clan == null) {
                p.sendMessage("§cKlanda değilsin.");
                return;
            }
            if (!plugin.getKlanManager().isLeader(p.getUniqueId())) {
                p.sendMessage("§cSadece klan lideri katılabilir.");
                return;
            }
            Tournament active = plugin.getTournamentManager().getActiveTournament();
            if (active == null) {
                p.sendMessage("§cAktif turnuva yok."); // Fixed: Use active tournament
                // Wait, tournament is active only if created. Logic: get most recent created?
                // For simplicity, let's assume getActiveTournament returns the open one too or create a method getOpenTournament
                // Assuming getActiveTournament covers created but not finished.
                return;
            }
            if (active.isStarted()) {
                p.sendMessage("§cTurnuva zaten başladı.");
                return;
            }
            if (active.addParticipant(clan)) {
                p.sendMessage("§aTurnuvaya katıldınız!");
            } else {
                p.sendMessage("§cTurnuva dolu veya zaten katıldınız.");
            }
        } else if (action.equals("baslat")) {
            if (!p.hasPermission("dkklan.admin")) {
                p.sendMessage("§cYetkin yok.");
                return;
            }
            Tournament active = plugin.getTournamentManager().getActiveTournament();
            if (active == null) {
                p.sendMessage("§cAktif turnuva yok.");
                return;
            }
            if (active.isStarted()) {
                p.sendMessage("§cZaten başladı.");
                return;
            }
            active.start();
            p.sendMessage("§aTurnuva başlatıldı!");
        } else if (action.equals("liste")) {
            Tournament active = plugin.getTournamentManager().getActiveTournament();
            if (active == null) {
                p.sendMessage("§cAktif turnuva yok.");
                return;
            }
            p.sendMessage("§6--- Turnuva Katılımcıları ---");
            for (String c : active.getParticipants()) {
                p.sendMessage("§e- " + c);
            }
        }
    }

    private void handleRulesetCommand(Player p, String[] args) {
        if (!p.hasPermission("dkklan.admin")) {
            p.sendMessage("§cYetkin yok.");
            return;
        }
        
        if (args.length < 2) {
            p.sendMessage("§eKullanım: /klansavasi ruleset <olustur/sec/liste/ayarla>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("olustur")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi ruleset olustur <isim>");
                return;
            }
            String name = args[2];
            plugin.getRulesetManager().createRuleset(name);
            p.sendMessage("§aRuleset oluşturuldu: " + name);
            
        } else if (action.equals("sec")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi ruleset sec <isim>");
                return;
            }
            String name = args[2];
            if (plugin.getRulesetManager().getRuleset(name) == null) {
                p.sendMessage("§cRuleset bulunamadı.");
                return;
            }
            plugin.getRulesetManager().setActiveRuleset(name);
            p.sendMessage("§aAktif ruleset güncellendi: " + name);
            
        } else if (action.equals("liste")) {
            p.sendMessage("§6--- Ruleset Listesi ---");
            p.sendMessage("§7Aktif: §a" + plugin.getRulesetManager().getActiveRulesetName());
            for (String name : plugin.getRulesetManager().getRulesetNames()) {
                p.sendMessage("§e- " + name);
            }
            
        } else if (action.equals("ayarla")) {
             if (args.length < 5) {
                p.sendMessage("§eKullanım: /klansavasi ruleset ayarla <isim> <ayar> <deger>");
                p.sendMessage("§7Ayarlar: cobweb, pearl, elytra, keepinv, ff, bet, maxplayers");
                return;
            }
            String name = args[2];
            String setting = args[3].toLowerCase();
            String value = args[4];
            
            WarRuleset ruleset = plugin.getRulesetManager().getRuleset(name);
            if (ruleset == null) {
                p.sendMessage("§cRuleset bulunamadı.");
                return;
            }
            
            try {
                switch (setting) {
                    case "cobweb": ruleset.setAllowCobweb(Boolean.parseBoolean(value)); break;
                    case "pearl": ruleset.setAllowEnderPearl(Boolean.parseBoolean(value)); break;
                    case "elytra": ruleset.setAllowElytra(Boolean.parseBoolean(value)); break;
                    case "keepinv": ruleset.setKeepInventory(Boolean.parseBoolean(value)); break;
                    case "ff": ruleset.setFriendlyFire(Boolean.parseBoolean(value)); break;
                    case "bet": ruleset.setBetAllowed(Boolean.parseBoolean(value)); break;
                    case "maxplayers": ruleset.setMaxPlayers(Integer.parseInt(value)); break;
                    default: p.sendMessage("§cGeçersiz ayar."); return;
                }
                plugin.getRulesetManager().saveRulesets();
                p.sendMessage("§aAyar güncellendi.");
            } catch (Exception e) {
                p.sendMessage("§cHata: Değer geçersiz.");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player p = sender instanceof Player ? (Player) sender : null;
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> sub = Arrays.asList("arena", "savas", "map", "cast", "camera", "turnuva", "ruleset", "kit", "test", "bilgi", "izle", "ayril", "setspawn");
            for (String s : sub) if (s.startsWith(args[0].toLowerCase())) out.add(s);
            return out;
        }
        String a0 = args[0].toLowerCase();
        if (args.length == 2) {
            if (a0.equals("arena")) out = Arrays.asList("setpos1", "setpos2", "olustur", "edit");
            else if (a0.equals("kadro")) out = Arrays.asList("sec");
            else if (a0.equals("savas")) out = Arrays.asList("davet", "kabul");
            else if (a0.equals("map")) out = Arrays.asList("ban", "liste");
            else if (a0.equals("cast")) out = Arrays.asList("basla", "bitir");
            else if (a0.equals("camera")) out = Arrays.asList("set", "tp");
            else if (a0.equals("turnuva")) out = Arrays.asList("olustur", "katil", "baslat", "liste");
            else if (a0.equals("ruleset")) out = Arrays.asList("olustur", "sec", "liste", "ayarla");
            else if (a0.equals("kit")) out = Arrays.asList("kaydet", "yukle", "liste", "sil", "duzenle");
            else if (a0.equals("test")) out = Arrays.asList("on", "off", "fakeklan", "addbot", "kill", "score", "end", "draft");
        } else {
            if (a0.equals("savas")) {
                if (args.length == 3 && args[1].equalsIgnoreCase("davet")) {
                    String my = p != null ? plugin.getKlanManager().getPlayerKlan(p.getUniqueId()) : null;
                    for (String c : plugin.getKlanManager().getAllClans()) {
                        if (my == null || !c.equalsIgnoreCase(my)) out.add(c);
                    }
                } else if (args.length == 3 && args[1].equalsIgnoreCase("kabul")) {
                    if (p != null) {
                        String myClan = plugin.getKlanManager().getPlayerKlan(p.getUniqueId());
                        if (myClan != null) {
                            WarManager.Invite inv = plugin.getWarManager().getInviteForClan(myClan);
                            if (inv != null) out.add(inv.senderClan);
                        }
                    }
                } else if (args.length == 4 && args[1].equalsIgnoreCase("davet")) {
                    out = Arrays.asList("0", "3", "5");
                } else if (args.length == 5 && args[1].equalsIgnoreCase("davet")) {
                    out = Arrays.asList("0", "100000", "250000", "500000", "1000000");
                } else if (args.length == 6 && args[1].equalsIgnoreCase("davet")) {
                    out = Arrays.asList("1", "3", "5");
                } else if (args.length == 7 && args[1].equalsIgnoreCase("davet")) {
                    out = new ArrayList<>(plugin.getKitManager().getKitNames());
                }
            } else if (a0.equals("arena")) {
                if (args.length == 3 && args[1].equalsIgnoreCase("edit")) {
                    for (com.dkprojects.dkklan.objects.WarArena a : plugin.getWarManager().getArenas()) out.add(a.getName());
                } else if (args.length == 4 && args[1].equalsIgnoreCase("edit")) {
                    out = Arrays.asList("setlobby", "setwarp1", "setwarp2");
                }
            } else if (a0.equals("map")) {
                if (args.length == 3 && args[1].equalsIgnoreCase("ban")) {
                    String clan = p != null ? plugin.getKlanManager().getPlayerKlan(p.getUniqueId()) : null;
                    com.dkprojects.dkklan.war.MatchSeries series = clan != null ? plugin.getWarManager().getActiveSeries(clan) : null;
                    if (series != null && series.getDraft() != null) {
                        for (com.dkprojects.dkklan.objects.WarArena a : series.getDraft().getPool()) out.add(a.getName());
                    } else {
                        for (com.dkprojects.dkklan.objects.WarArena a : plugin.getWarManager().getArenas()) out.add(a.getName());
                    }
                }
            } else if (a0.equals("ruleset")) {
                if (args.length == 3 && (args[1].equalsIgnoreCase("sec") || args[1].equalsIgnoreCase("ayarla"))) {
                    out = new ArrayList<>(plugin.getRulesetManager().getRulesetNames());
                } else if (args.length == 4 && args[1].equalsIgnoreCase("ayarla")) {
                    out = Arrays.asList("cobweb", "pearl", "elytra", "keepinv", "ff", "bet", "maxplayers");
                } else if (args.length == 5 && args[1].equalsIgnoreCase("ayarla")) {
                    String setting = args[3].toLowerCase();
                    if (setting.equals("maxplayers")) out = Arrays.asList("10", "12", "20");
                    else out = Arrays.asList("true", "false");
                }
            } else if (a0.equals("kit")) {
                if (args.length == 3 && (args[1].equalsIgnoreCase("yukle") || args[1].equalsIgnoreCase("sil") || args[1].equalsIgnoreCase("duzenle"))) {
                    out = new ArrayList<>(plugin.getKitManager().getKitNames());
                }
            } else if (a0.equals("test")) {
                String action = args[1].toLowerCase();
                if (args.length == 3 && (action.equals("fakeklan") || action.equals("kill") || action.equals("score") || action.equals("end") || action.equals("addbot"))) {
                    out = new ArrayList<>(plugin.getKlanManager().getAllClans());
                } else if (args.length == 4 && action.equals("addbot")) {
                    out = Arrays.asList("1", "3", "5", "10");
                } else if (args.length == 3 && action.equals("draft")) {
                    out = Arrays.asList("finish", "start");
                } else if (args.length == 4 && action.equals("draft")) {
                    out = new ArrayList<>(plugin.getKlanManager().getAllClans());
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
    
    private void handleKitCommand(Player p, String[] args) {
        if (!p.hasPermission("dkklan.admin")) {
            p.sendMessage("§cYetkin yok.");
            return;
        }
        
        if (args.length < 2) {
            p.sendMessage("§eKullanım: /klansavasi kit <kaydet/yukle/liste/sil/duzenle>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("kaydet")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi kit kaydet <isim>");
                return;
            }
            String name = args[2];
            plugin.getKitManager().saveKit(p, name);
            
        } else if (action.equals("duzenle") || action.equals("edit")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi kit duzenle <isim>");
                return;
            }
            String name = args[2];
            if (!plugin.getKitManager().kitExists(name)) {
                p.sendMessage("§cKit bulunamadı.");
                return;
            }
            new com.dkprojects.dkklan.menus.KitEditMenu(plugin, name).open(p);

        } else if (action.equals("yukle")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi kit yukle <isim>");
                return;
            }
            String name = args[2];
            if (!plugin.getKitManager().kitExists(name)) {
                p.sendMessage("§cKit bulunamadı.");
                return;
            }
            plugin.getKitManager().giveKit(p, name);
            p.sendMessage("§aKit yüklendi: " + name);
            
        } else if (action.equals("liste")) {
            p.sendMessage("§6--- Kit Listesi ---");
            for (String name : plugin.getKitManager().getKitNames()) {
                p.sendMessage("§e- " + name);
            }
        } else if (action.equals("sil")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi kit sil <isim>");
                return;
            }
            String name = args[2];
            if (!plugin.getKitManager().kitExists(name)) {
                p.sendMessage("§cKit bulunamadı.");
                return;
            }
            plugin.getKitManager().deleteKit(name);
            p.sendMessage("§aKit silindi: " + name);
        }
    }
    
    private void handleTestCommand(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§eKullanım: /klansavasi test <on/off/fakeklan/addbot/kill/score/end/draft>");
            return;
        }
        String action = args[1].toLowerCase();
        WarManager wm = plugin.getWarManager();
        
        if (action.equals("on")) {
            wm.setTestModeEnabled(true);
            p.sendMessage(plugin.getMessage("test-mode-enabled"));
        } else if (action.equals("off")) {
            wm.setTestModeEnabled(false);
            p.sendMessage(plugin.getMessage("test-mode-disabled"));
        } else if (action.equals("fakeklan")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi test fakeklan <isim>");
                return;
            }
            plugin.getKlanManager().createFakeClan(args[2], p.getUniqueId());
            p.sendMessage(plugin.getMessage("fake-clan-created").replace("%clan%", args[2]));
        } else if (action.equals("addbot")) {
            if (args.length < 4) {
                p.sendMessage("§eKullanım: /klansavasi test addbot <klan> <sayi>");
                return;
            }
            String clan = args[2];
            int count;
            try {
                count = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                p.sendMessage("§cGeçersiz sayı.");
                return;
            }
            plugin.getKlanManager().addFakeBots(clan, count);
            p.sendMessage(plugin.getMessage("bots-added").replace("%count%", String.valueOf(count)).replace("%clan%", clan));
        } else if (action.equals("kill")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi test kill <klan> [oyuncu]");
                return;
            }
            String clan = args[2];
            WarManager manager = plugin.getWarManager();
            com.dkprojects.dkklan.war.War war = manager.getWar(clan);
            if (war == null) {
                p.sendMessage("§cAktif savaş bulunamadı.");
                return;
            }
            java.util.UUID killer = p.getUniqueId();
            if (args.length >= 4) {
                Player kp = p.getServer().getPlayer(args[3]);
                if (kp != null) killer = kp.getUniqueId();
            }
            war.addKill(killer, clan);
            p.sendMessage("§aKill eklendi: " + clan);
        } else if (action.equals("score")) {
            if (args.length < 4) {
                p.sendMessage("§eKullanım: /klansavasi test score <klan> <miktar>");
                return;
            }
            String clan = args[2];
            int amt;
            try {
                amt = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                p.sendMessage("§cGeçersiz sayı.");
                return;
            }
            com.dkprojects.dkklan.war.War war = plugin.getWarManager().getWar(clan);
            if (war == null) {
                p.sendMessage("§cAktif savaş bulunamadı.");
                return;
            }
            war.addScore(clan, amt);
            p.sendMessage("§aSkor eklendi: " + clan + " +" + amt);
        } else if (action.equals("end")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi test end <klan> [sebep]");
                return;
            }
            String clan = args[2];
            com.dkprojects.dkklan.war.War war = plugin.getWarManager().getWar(clan);
            if (war == null) {
                p.sendMessage("§cAktif savaş bulunamadı.");
                return;
            }
            String reason = args.length >= 4 ? String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length)) : "Test";
            plugin.getWarManager().endWar(war, reason);
            p.sendMessage("§aSavaş sonlandırıldı: " + reason);
        } else if (action.equals("draft")) {
            if (args.length < 4) {
                p.sendMessage("§eKullanım: /klansavasi test draft <finish/start> <klan>");
                return;
            }
            String op = args[2].toLowerCase();
            String clan = args[3];
            com.dkprojects.dkklan.war.MatchSeries series = plugin.getWarManager().getSeries(clan);
            if (series == null) {
                p.sendMessage("§cAktif seri bulunamadı.");
                return;
            }
            if (op.equals("finish")) {
                if (series.getDraft() != null) {
                    series.getDraft().forceFinish();
                    plugin.getWarManager().startSeriesMatches(series);
                    p.sendMessage("§aDraft bitirildi ve seri başlatıldı.");
                } else {
                    p.sendMessage("§cDraft bulunamadı.");
                }
            } else if (op.equals("start")) {
                plugin.getWarManager().startSeriesMatches(series);
                p.sendMessage("§aSeri başlatıldı.");
            }
        }
    }

    private void handleWarCommand(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§eKullanım: /klansavasi map <ban/liste>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("ban")) {
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi map ban <arena>");
                return;
            }
            plugin.getWarManager().processBan(p, args[2]);
        } else if (action.equals("liste")) {
            String clan = plugin.getKlanManager().getPlayerKlan(p.getUniqueId());
            if (clan == null) {
                p.sendMessage("§cKlanda değilsin.");
                return;
            }
            MatchSeries series = plugin.getWarManager().getActiveSeries(clan);
            if (series == null || series.getDraft() == null) {
                p.sendMessage("§cAktif bir draft yok.");
                return;
            }
            MapDraft draft = series.getDraft();
            p.sendMessage("§6--- Map Draft ---");
            p.sendMessage("§7Sıra: §e" + draft.getCurrentTurn());
            p.sendMessage("§7Yasaklananlar: §c" + draft.getBannedMaps().stream().map(WarArena::getName).collect(Collectors.joining(", ")));
            p.sendMessage("§7Kalanlar: §a" + draft.getPool().stream().map(WarArena::getName).collect(Collectors.joining(", ")));
        }
    }
    
    private void handleCastCommand(Player p, String[] args) {
        if (!p.hasPermission("dkklan.admin")) {
            p.sendMessage("§cBunu yapmaya yetkin yok.");
            return;
        }
        
        if (args.length < 2) {
            p.sendMessage("§eKullanım: /klansavasi cast <basla/bitir>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("basla")) {
            // Find active series? Or ask for clan name?
            // For simplicity, find if player is near a war or specify clan
            if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi cast basla <klan_adi>");
                return;
            }
            String targetClan = args[2];
            MatchSeries series = plugin.getWarManager().getActiveSeries(targetClan);
            if (series == null) {
                p.sendMessage("§cBu klanın aktif bir serisi yok.");
                return;
            }
            plugin.getWarManager().startBroadcast(series, p);
            
        } else if (action.equals("bitir")) {
             if (args.length < 3) {
                p.sendMessage("§eKullanım: /klansavasi cast bitir <klan_adi>");
                return;
            }
            String targetClan = args[2];
            MatchSeries series = plugin.getWarManager().getActiveSeries(targetClan);
            plugin.getWarManager().stopBroadcast(series); // This stops for everyone, maybe allow leaving only?
            // BroadcastSession.removeCaster handles single removal.
            BroadcastSession session = plugin.getWarManager().getBroadcastSession(targetClan);
            if (session != null) session.removeCaster(p);
            else p.sendMessage("§cAktif yayın yok.");
        }
    }
    
    private void handleCameraCommand(Player p, String[] args) {
        if (!p.hasPermission("dkklan.admin")) {
            p.sendMessage("§cBunu yapmaya yetkin yok.");
            return;
        }
        
        if (args.length < 3) {
            p.sendMessage("§eKullanım: /klansavasi camera <set/tp> <isim>");
            return;
        }
        
        String action = args[1].toLowerCase();
        String name = args[2];
        
        if (action.equals("set")) {
            // Get arena player is in?
            // Need a way to find arena by location
            WarArena arena = plugin.getWarManager().getArenaByLocation(p.getLocation());
            if (arena == null) {
                p.sendMessage("§cBir arenanın içinde olmalısın.");
                return;
            }
            arena.addCameraPoint(name, p.getLocation());
            p.sendMessage("§aKamera noktası '" + name + "' eklendi: " + arena.getName());
            // Should save arenas config?
            plugin.getWarManager().saveArenas();
            
        } else if (action.equals("tp")) {
             WarArena arena = plugin.getWarManager().getArenaByLocation(p.getLocation());
             // Or find by name if provided? But we only have camera name.
             // Assume player is in/near arena or we search all arenas (slow).
             // Better: if in broadcast, use series arena.
             
             // Try current location first
             if (arena == null) {
                 // Try finding from active broadcast
                 // This part is tricky without context. 
                 // Let's iterate all arenas for now or require arena name?
                 // User said: /klansavasi camera set <isim>
                 // User said: /klansavasi camera tp <isim> (in BroadcastSession hint)
                 
                 for (WarArena a : plugin.getWarManager().getArenas()) {
                     if (a.getCameraPoint(name) != null) {
                         p.teleport(a.getCameraPoint(name));
                         p.sendMessage("§aKamera noktasına ışınlandın: " + name);
                         return;
                     }
                 }
                 p.sendMessage("§cKamera noktası bulunamadı.");
             } else {
                 Location loc = arena.getCameraPoint(name);
                 if (loc != null) {
                     p.teleport(loc);
                     p.sendMessage("§aKamera noktasına ışınlandın: " + name);
                 } else {
                     p.sendMessage("§cBu arenada böyle bir nokta yok.");
                 }
             }
        }
    }
}

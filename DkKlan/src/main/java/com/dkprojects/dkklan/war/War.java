package com.dkprojects.dkklan.war;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.WarArena;
import com.dkprojects.dkklan.objects.WarRuleset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class War {
    private final WarTeam teamA;
    private final WarTeam teamB;
    private final WarArena arena;
    private final long startTime;
    private WarState state;
    private final double bet;
    private final int maxDuration;
    
    private WarType type = WarType.CLASSIC;
    private int targetPlayerCount = 0; // 0 = unlimited/all
    
    private WarRuleset ruleset;

    private String kitName; // Selected kit
    private Map<UUID, org.bukkit.inventory.ItemStack[]> savedInventories = new HashMap<>(); // Original inventories
    private Map<UUID, org.bukkit.inventory.ItemStack[]> savedArmor = new HashMap<>();
    
    // Stats per player for MVP calculation
    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();

    // Border variables
    private final double centerX;
    private final double centerZ;
    private final double initialRadiusX;
    private final double initialRadiusZ;
    
    private BukkitTask warTask;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private final Map<UUID, org.bukkit.scoreboard.Scoreboard> scoreboards = new ConcurrentHashMap<>();
    private final Map<UUID, org.bukkit.scoreboard.Objective> objectives = new ConcurrentHashMap<>();

    public War(String clanA, String clanB, int duration, double bet, WarArena arena, List<UUID> playersA, List<UUID> playersB) {
        this(clanA, clanB, duration, bet, arena, playersA, playersB, WarType.CLASSIC, 0);
    }

    public War(String clanA, String clanB, int duration, double bet, WarArena arena, List<UUID> playersA, List<UUID> playersB, WarType type, int targetPlayerCount) {
        this.teamA = new WarTeam(clanA, playersA);
        this.teamB = new WarTeam(clanB, playersB);
        this.arena = arena;
        this.maxDuration = duration;
        this.startTime = System.currentTimeMillis();
        this.state = WarState.WAITING;
        this.bet = bet;
        this.type = type;
        this.targetPlayerCount = targetPlayerCount;
        
        // Default ruleset if not set
        this.ruleset = new WarRuleset("Default");

        // Calculate center and radius from arena
        Location pos1 = arena.getPos1();
        Location pos2 = arena.getPos2();
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        this.centerX = (minX + maxX) / 2.0;
        this.centerZ = (minZ + maxZ) / 2.0;
        this.initialRadiusX = (maxX - minX) / 2.0;
        this.initialRadiusZ = (maxZ - minZ) / 2.0;
        
        // Initialize stats
        for (UUID uuid : playersA) playerStats.put(uuid, new PlayerStats());
        for (UUID uuid : playersB) playerStats.put(uuid, new PlayerStats());
    }
    
    public void setRuleset(WarRuleset ruleset) {
        this.ruleset = ruleset;
    }

    public String getKitName() { return kitName; }
    public void setKitName(String kitName) { this.kitName = kitName; }
    
    public void saveOriginalInventory(Player p) {
        savedInventories.put(p.getUniqueId(), p.getInventory().getContents());
        savedArmor.put(p.getUniqueId(), p.getInventory().getArmorContents());
    }
    
    public void restoreOriginalInventory(Player p) {
        if (savedInventories.containsKey(p.getUniqueId())) {
            p.getInventory().setContents(savedInventories.get(p.getUniqueId()));
            p.getInventory().setArmorContents(savedArmor.get(p.getUniqueId()));
            
            savedInventories.remove(p.getUniqueId());
            savedArmor.remove(p.getUniqueId());
        }
    }

    public WarRuleset getRuleset() {
        return ruleset;
    }
    
    public void startWarTask(DkKlan plugin) {
        warTask = Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 0L, 10L); // Every 0.5s
        
        // Initialize WorldBorder
        if (arena.getPos1().getWorld() != null) {
            org.bukkit.WorldBorder wb = arena.getPos1().getWorld().getWorldBorder();
            wb.setCenter(centerX, centerZ);
            // Calculate start size dynamically: Max dimension + 100 (50 blocks buffer on each side)
            double maxDimension = Math.max(initialRadiusX * 2, initialRadiusZ * 2);
            double startSize = maxDimension + 100.0;
            
            // double startSize = DkKlan.getInstance().getConfig().getDouble("wars.border.start-size", initialRadiusX * 2);
            double endSize = DkKlan.getInstance().getConfig().getDouble("wars.border.end-size", 20.0);
            // Default shrink time is now faster (120s) instead of full duration
            int shrinkSeconds = DkKlan.getInstance().getConfig().getInt("wars.border.shrink-seconds", 120); 
            boolean borderEnabled = DkKlan.getInstance().getConfig().getBoolean("wars.border.enabled", true);
            if (borderEnabled) {
                wb.setSize(startSize);
                wb.setSize(endSize, shrinkSeconds);
            }
        }
        
        boolean sbEnabled = DkKlan.getInstance().getConfig().getBoolean("wars.scoreboard.enabled", true);
        if (sbEnabled) {
            for (UUID uuid : getParticipants()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                org.bukkit.scoreboard.ScoreboardManager sm = Bukkit.getScoreboardManager();
                org.bukkit.scoreboard.Scoreboard sb = sm.getNewScoreboard();
                String title = org.bukkit.ChatColor.translateAlternateColorCodes('&', DkKlan.getInstance().getConfig().getString("wars.scoreboard.title", "&6&lKLAN SAVAŞI"));
                org.bukkit.scoreboard.Objective obj = sb.registerNewObjective("dkwar", "dummy", title);
                obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
                scoreboards.put(uuid, sb);
                objectives.put(uuid, obj);
                p.setScoreboard(sb);
            }
        }
    }

    public void stopWarTask() {
        if (warTask != null) {
            warTask.cancel();
            warTask = null;
        }
        bossBars.values().forEach(BossBar::removeAll);
        bossBars.clear();
        for (UUID uuid : scoreboards.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        scoreboards.clear();
        objectives.clear();
        
        // Reset WorldBorder (to a large default)
        if (arena.getPos1().getWorld() != null) {
             org.bukkit.WorldBorder wb = arena.getPos1().getWorld().getWorldBorder();
             wb.setSize(60000000); // Default Minecraft size
        }
    }

    private boolean suddenDeath = false;
    
    // ...
    
    private void onTick() {
        if (state != WarState.STARTED) return;
        
        // Time Check
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsed >= maxDuration && !suddenDeath) {
            if (getScoreA() == getScoreB()) {
                activateSuddenDeath();
            } else {
                com.dkprojects.dkklan.DkKlan.getInstance().getWarManager().endWar(this, "Süre doldu.");
            }
        }
        
        updateDirections();
        updateScoreboards(elapsed);
        
        // Visualize border with particles (optional)
        boolean particles = DkKlan.getInstance().getConfig().getBoolean("wars.border.particles", true);
        if (particles && System.currentTimeMillis() % 2000 < 500) {
            World world = arena.getPos1().getWorld();
            if (world != null) {
                double size = world.getWorldBorder().getSize();
                double r = size / 2.0;
                double y = arena.getLobby().getY();
                
                for (int i = 0; i < 4; i++) { // 4 corners
                     double cx = (i % 2 == 0 ? 1 : -1) * r;
                     double cz = (i < 2 ? 1 : -1) * r;
                     world.spawnParticle(org.bukkit.Particle.FLAME, centerX + cx, y + 1, centerZ + cz, 5, 0.1, 0.5, 0.1, 0.05);
                }
            }
        }
    }
    
    private void activateSuddenDeath() {
        suddenDeath = true;
        FileConfiguration config = DkKlan.getInstance().getConfig();
        Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', config.getString("messages.war-sudden-death-broadcast", "&c☠ §lSUDDEN DEATH! §c☠")));
        Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', config.getString("messages.war-sudden-death-msg1", "&eSüre doldu ve skorlar eşit!")));
        Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', config.getString("messages.war-sudden-death-msg2", "&6İlk skoru alan kazanır! Arena daralıyor!")));
        
        if (arena.getPos1().getWorld() != null) {
            double sdSize = DkKlan.getInstance().getConfig().getDouble("wars.border.sudden-death-size", 5.0);
            int sdSeconds = DkKlan.getInstance().getConfig().getInt("wars.border.sudden-death-seconds", 10);
            arena.getPos1().getWorld().getWorldBorder().setSize(sdSize, sdSeconds);
        }
        
        for (UUID uuid : getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(org.bukkit.ChatColor.translateAlternateColorCodes('&', config.getString("messages.war-sudden-death-title", "&c☠ SUDDEN DEATH ☠")), 
                            org.bukkit.ChatColor.translateAlternateColorCodes('&', config.getString("messages.war-sudden-death-subtitle", "&eİLK VURAN KAZANIR!")), 10, 60, 20);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
            }
        }
    }
    
    public boolean isSuddenDeath() { return suddenDeath; }

    // Removed manual border calculation methods

    public void checkElimination() {
        long aliveA = teamA.getPlayers().stream().map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline() && !p.isDead() && p.getGameMode() != org.bukkit.GameMode.SPECTATOR)
                .count();
                
        long aliveB = teamB.getPlayers().stream().map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline() && !p.isDead() && p.getGameMode() != org.bukkit.GameMode.SPECTATOR)
                .count();
        
        if (aliveA == 0 && aliveB == 0) {
            // Draw or last man standing logic (usually impossible if processed sequentially, but can happen)
            com.dkprojects.dkklan.DkKlan.getInstance().getWarManager().endWar(this, "Her iki takım da elendi (Berabere)");
        } else if (aliveA == 0) {
            com.dkprojects.dkklan.DkKlan.getInstance().getWarManager().endWar(this, "Rakip takım elendi! Kazanan: " + teamB.getClanName());
        } else if (aliveB == 0) {
            com.dkprojects.dkklan.DkKlan.getInstance().getWarManager().endWar(this, "Rakip takım elendi! Kazanan: " + teamA.getClanName());
        }
    }

    private void updateDirections() {
        updateTeamDirections(teamA, teamB);
        updateTeamDirections(teamB, teamA);
    }

    private void updateTeamDirections(WarTeam myTeam, WarTeam enemyTeam) {
        for (UUID uuid : myTeam.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;
            
            Player nearest = null;
            double minDist = Double.MAX_VALUE;
            
            for (UUID enemyId : enemyTeam.getPlayers()) {
                Player enemy = Bukkit.getPlayer(enemyId);
                if (enemy != null && enemy.isOnline() && !enemy.isDead() && enemy.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                    if (!p.getWorld().equals(enemy.getWorld())) continue;
                    double dist = p.getLocation().distanceSquared(enemy.getLocation());
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = enemy;
                    }
                }
            }
            
            BossBar bar = bossBars.computeIfAbsent(uuid, k -> {
                BossBar b = Bukkit.createBossBar("§fSavaş Başlıyor...", BarColor.RED, BarStyle.SOLID);
                b.addPlayer(p);
                return b;
            });
            
            if (nearest != null) {
                String arrow = getArrow(p, nearest.getLocation());
                int dist = (int) Math.sqrt(minDist);
                bar.setTitle("§c⚔ §fHedef: §e" + nearest.getName() + " §7| §fMesafe: §b" + dist + "m §6" + arrow);
                bar.setProgress(1.0);
            } else {
                bar.setTitle("§aTüm rakipler elendi!");
                bar.setProgress(1.0);
            }
        }
    }

    private String getArrow(Player p, Location target) {
        Location loc = p.getLocation();
        double angleToTarget = Math.atan2(target.getZ() - loc.getZ(), target.getX() - loc.getX());
        double angleToTargetDeg = Math.toDegrees(angleToTarget) - 90; 
        double playerYaw = loc.getYaw();
        double diff = angleToTargetDeg - playerYaw;
        
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        
        if (diff >= -22.5 && diff < 22.5) return "⬆";
        if (diff >= 22.5 && diff < 67.5) return "⬈";
        if (diff >= 67.5 && diff < 112.5) return "➡";
        if (diff >= 112.5 && diff < 157.5) return "⬊";
        if (diff >= 157.5 || diff < -157.5) return "⬇";
        if (diff >= -157.5 && diff < -112.5) return "⬋";
        if (diff >= -112.5 && diff < -67.5) return "⬅";
        if (diff >= -67.5 && diff < -22.5) return "⬉";
        
        return "⬆";
    }
    
    private Player getNearestEnemy(Player p) {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        java.util.List<UUID> enemies = new java.util.ArrayList<>();
        enemies.addAll(teamA.getPlayers());
        enemies.addAll(teamB.getPlayers());
        for (UUID id : enemies) {
            Player e = Bukkit.getPlayer(id);
            if (e != null && e.isOnline() && !e.isDead() && e.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                if (!p.getWorld().equals(e.getWorld())) continue;
                double d = p.getLocation().distanceSquared(e.getLocation());
                if (d < minDist) {
                    minDist = d;
                    nearest = e;
                }
            }
        }
        return nearest;
    }
    
    public double getBet() { return bet; }
    public WarArena getArena() { return arena; }

    public void addKill(UUID killer, String clan) {
        if (teamA.getClanName().equals(clan)) {
            teamA.addKill();
        } else if (teamB.getClanName().equals(clan)) {
            teamB.addKill();
        }
        getPlayerStats(killer).addKill();
        
        if (suddenDeath) {
            com.dkprojects.dkklan.DkKlan.getInstance().getWarManager().endWar(this, "Sudden Death Sonucu");
        }
    }
    
    public void addScore(String clan, int amount) {
        if (teamA.getClanName().equals(clan)) {
            teamA.addScore(amount);
        } else if (teamB.getClanName().equals(clan)) {
            teamB.addScore(amount);
        }
    }
    
    public void addDeath(UUID victim, String clan) {
        if (teamA.getClanName().equals(clan)) {
            teamA.addDeath();
        } else if (teamB.getClanName().equals(clan)) {
            teamB.addDeath();
        }
        getPlayerStats(victim).addDeath();
    }
    
    public PlayerStats getPlayerStats(UUID uuid) {
        return playerStats.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    public String getClanA() { return teamA.getClanName(); }
    public String getClanB() { return teamB.getClanName(); }
    public WarTeam getTeamA() { return teamA; }
    public WarTeam getTeamB() { return teamB; }
    
    public int getScoreA() { return teamA.getScore(); }
    public int getScoreB() { return teamB.getScore(); }
    
    public WarState getState() { return state; }
    public void setState(WarState state) { this.state = state; }
    public boolean isActive() { return state == WarState.STARTED; }
    
    public List<UUID> getParticipants() {
        List<UUID> all = new ArrayList<>(teamA.getPlayers());
        all.addAll(teamB.getPlayers());
        return all;
    }
    
    public Map<UUID, PlayerStats> getAllPlayerStats() { return playerStats; }
    
    public WarType getType() { return type; }
    public int getTargetPlayerCount() { return targetPlayerCount; }
    
    public boolean addPlayerToTeam(UUID uuid, String clanName) {
        if (teamA.getClanName().equals(clanName)) {
            if (teamA.getPlayers().contains(uuid)) return false;
            teamA.getPlayers().add(uuid);
            playerStats.put(uuid, new PlayerStats());
            return true;
        } else if (teamB.getClanName().equals(clanName)) {
            if (teamB.getPlayers().contains(uuid)) return false;
            teamB.getPlayers().add(uuid);
            playerStats.put(uuid, new PlayerStats());
            return true;
        }
        return false;
    }
    
    public boolean isRosterFull() {
        if (targetPlayerCount <= 0) return true;
        return teamA.getPlayers().size() >= targetPlayerCount && teamB.getPlayers().size() >= targetPlayerCount;
    }
    
    public void removePlayer(UUID uuid, String clanName) {
        if (teamA.getClanName().equals(clanName)) {
            teamA.removePlayer(uuid);
        } else if (teamB.getClanName().equals(clanName)) {
            teamB.removePlayer(uuid);
        }
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }
    
    public void checkLoadouts() {
        for (UUID uuid : getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                // Check illegal items
                if (p.getInventory().contains(org.bukkit.Material.ELYTRA) || 
                    (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() == org.bukkit.Material.ELYTRA)) {
                    p.getInventory().remove(org.bukkit.Material.ELYTRA);
                    if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() == org.bukkit.Material.ELYTRA) {
                        p.getInventory().setChestplate(null);
                    }
                    p.sendMessage("§cElytra bu savaşta yasak! Envanterinden silindi.");
                }
                
                if (p.getInventory().contains(org.bukkit.Material.ENDER_PEARL)) {
                     p.getInventory().remove(org.bukkit.Material.ENDER_PEARL);
                     p.sendMessage("§cEnder Pearl bu savaşta yasak! Envanterinden silindi.");
                }
            }
        }
    }
    
    public UUID calculateMVP() {
        UUID mvp = null;
        int maxScore = Integer.MIN_VALUE;
        for (Map.Entry<UUID, PlayerStats> entry : playerStats.entrySet()) {
            int score = entry.getValue().getScore();
            if (score > maxScore) {
                maxScore = score;
                mvp = entry.getKey();
            }
        }
        return mvp;
    }
    
    private void updateScoreboards(long elapsed) {
        boolean sbEnabled = DkKlan.getInstance().getConfig().getBoolean("wars.scoreboard.enabled", true);
        if (!sbEnabled) return;
        
        FileConfiguration config = DkKlan.getInstance().getConfig();
        int remaining = Math.max(0, maxDuration - (int) elapsed);
        boolean showTime = config.getBoolean("wars.scoreboard.show-time", true);
        
        String versusFmt = config.getString("wars.scoreboard.lines.versus", "&e%clanA% &7vs &c%clanB%");
        String scoreFmt = config.getString("wars.scoreboard.lines.score", "&aSkor: &b%scoreA% &7- &c%scoreB%");
        String timeFmt = config.getString("wars.scoreboard.lines.time", "&eSüre: &f%time% sn");
        String targetFmt = config.getString("wars.scoreboard.lines.target", "&eHedef: &f%target%");
        String distanceFmt = config.getString("wars.scoreboard.lines.distance", "&eMesafe: &f%distance%m");
        
        // Prepare common placeholders
        String versusLine = org.bukkit.ChatColor.translateAlternateColorCodes('&', versusFmt
                .replace("%clanA%", getClanA()).replace("%clanB%", getClanB()));
        String scoreLine = org.bukkit.ChatColor.translateAlternateColorCodes('&', scoreFmt
                .replace("%scoreA%", String.valueOf(getScoreA())).replace("%scoreB%", String.valueOf(getScoreB())));
        String timeLine = showTime ? org.bukkit.ChatColor.translateAlternateColorCodes('&', timeFmt
                .replace("%time%", String.valueOf(remaining))) : "";

        for (UUID uuid : getParticipants()) {
            org.bukkit.scoreboard.Objective obj = objectives.get(uuid);
            org.bukkit.scoreboard.Scoreboard sb = scoreboards.get(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (obj == null || sb == null || p == null) continue;
            
            for (String e : sb.getEntries()) sb.resetScores(e);
            
            obj.getScore(versusLine).setScore(3);
            obj.getScore(scoreLine).setScore(2);
            if (!timeLine.isEmpty()) obj.getScore(timeLine).setScore(1);
        }
        
        com.dkprojects.dkklan.managers.WarManager wm = DkKlan.getInstance().getWarManager();
        String warId = getClanA() + "_" + getClanB();
        for (java.util.Map.Entry<UUID, String> entry : wm.getSpectators().entrySet()) {
            if (!warId.equals(entry.getValue())) continue;
            UUID uuid = entry.getKey();
            Player sp = Bukkit.getPlayer(uuid);
            if (sp == null) continue;
            org.bukkit.scoreboard.Scoreboard sb = wm.getSpectatorScoreboard(uuid);
            if (sb == null) {
                org.bukkit.scoreboard.ScoreboardManager sm = Bukkit.getScoreboardManager();
                if (sm == null) continue;
                sb = sm.getNewScoreboard();
                String title = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        config.getString("wars.scoreboard.spectator-title", "&6&lSAVAŞ İZLEME"));
                org.bukkit.scoreboard.Objective obj = sb.registerNewObjective("dkspectate", "dummy", title);
                obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
                wm.setSpectatorScoreboard(uuid, sb);
                sp.setScoreboard(sb);
            }
            org.bukkit.scoreboard.Objective obj = sb.getObjective("dkspectate");
            if (obj == null) continue;
            for (String e : sb.getEntries()) sb.resetScores(e);
            
            Player nearest = getNearestEnemy(sp);
            String targetLine = org.bukkit.ChatColor.translateAlternateColorCodes('&', targetFmt
                    .replace("%target%", nearest != null ? nearest.getName() : "Yok"));
            String distLine = "";
            if (nearest != null) {
                 distLine = org.bukkit.ChatColor.translateAlternateColorCodes('&', distanceFmt
                    .replace("%distance%", String.valueOf((int) sp.getLocation().distance(nearest.getLocation()))));
            }
            
            obj.getScore(versusLine).setScore(5);
            obj.getScore(scoreLine).setScore(4);
            if (!timeLine.isEmpty()) obj.getScore(timeLine).setScore(3);
            obj.getScore(targetLine).setScore(2);
            if (!distLine.isEmpty()) obj.getScore(distLine).setScore(1);
        }
    }

    public static class PlayerStats {
        private int kills;
        private int deaths;
        public void addKill() { kills++; }
        public void addDeath() { deaths++; }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
        public int getScore() { return (kills * 2) - deaths; }
    }
}

package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KlanManager {
    private DkKlan plugin;
    private Connection connection;
    private final Object lock = new Object();
    
    // Caches
    private ConcurrentHashMap<String, String> playerKlan = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<String>> klanMembers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Double> klanBankCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> klanLevels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> klanMemberLimits = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> klanBankLimits = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> klanLeaders = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Location> klanBases = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> klanElo = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> klanLeague = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ClanRole> roleCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, String> pendingInvites = new ConcurrentHashMap<>();
    
    private Set<UUID> clanChatMode = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> fakeClans = Collections.synchronizedSet(new HashSet<>());
    private Set<UUID> inviteMode = Collections.synchronizedSet(new HashSet<>());
    private Set<UUID> createClanMode = Collections.synchronizedSet(new HashSet<>());
    private Set<UUID> announcementMode = Collections.synchronizedSet(new HashSet<>());

    public KlanManager(DkKlan plugin) {
        this.plugin = plugin;
        initializeDatabase();
        loadCache();
    }

    private void initializeDatabase() {
        synchronized (lock) {
            try {
                File dataFolder = new File(plugin.getDataFolder(), "database.db");
                if (!dataFolder.getParentFile().exists()) dataFolder.getParentFile().mkdirs();
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());
                
                try (Statement s = connection.createStatement()) {
                    s.execute("CREATE TABLE IF NOT EXISTS klans (" +
                            "name VARCHAR(16) PRIMARY KEY, " +
                            "leader VARCHAR(36) NOT NULL, " +
                            "level INT DEFAULT 1, " +
                            "bank DOUBLE DEFAULT 0, " +
                            "member_limit_level INT DEFAULT 1, " +
                            "bank_limit_level INT DEFAULT 1, " +
                            "base TEXT, " +
                            "elo INT DEFAULT 1000, " +
                            "league VARCHAR(16) DEFAULT 'Bronze')");

                    s.execute("CREATE TABLE IF NOT EXISTS klan_members (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "klan_name VARCHAR(16) NOT NULL, " +
                            "role VARCHAR(10) DEFAULT 'MEMBER')");
                            
                    s.execute("CREATE TABLE IF NOT EXISTS clan_wars (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "clan_a VARCHAR(16), " +
                            "clan_b VARCHAR(16), " +
                            "winner VARCHAR(16), " +
                            "score_a INT, " +
                            "score_b INT, " +
                            "start_time LONG, " +
                            "end_time LONG)");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void loadCache() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM klans")) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        klanLeaders.put(name, rs.getString("leader"));
                        klanLevels.put(name, rs.getInt("level"));
                        klanBankCache.put(name, rs.getDouble("bank"));
                        klanMemberLimits.put(name, rs.getInt("member_limit_level"));
                        klanBankLimits.put(name, rs.getInt("bank_limit_level"));
                        klanElo.put(name, rs.getInt("elo"));
                        String league = rs.getString("league");
                        klanLeague.put(name, league != null ? league : "Bronze");
                        if (rs.getString("base") != null) {
                            klanBases.put(name, deserializeLocation(rs.getString("base")));
                        }
                        klanMembers.put(name, new ArrayList<>());
                    }
                } catch (SQLException e) { e.printStackTrace(); }
                
                try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM klan_members")) {
                    while (rs.next()) {
                        String uuid = rs.getString("uuid");
                        String klan = rs.getString("klan_name");
                        playerKlan.put(uuid, klan);
                        if (klanMembers.containsKey(klan)) {
                            klanMembers.get(klan).add(uuid);
                        }
                    }
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    public boolean isFakeClan(String name) {
        return fakeClans.contains(name);
    }
    
    public void setInviteMode(UUID uuid, boolean mode) {
        if (mode) inviteMode.add(uuid);
        else inviteMode.remove(uuid);
    }
    
    public boolean isInviteMode(UUID uuid) {
        return inviteMode.contains(uuid);
    }
    
    public void setCreateClanMode(UUID uuid, boolean mode) {
        if (mode) createClanMode.add(uuid);
        else createClanMode.remove(uuid);
    }
    
    public boolean isCreateClanMode(UUID uuid) {
        return createClanMode.contains(uuid);
    }
    
    public void setAnnouncementMode(UUID uuid, boolean mode) {
        if (mode) announcementMode.add(uuid);
        else announcementMode.remove(uuid);
    }

    public boolean isAnnouncementMode(UUID uuid) {
        return announcementMode.contains(uuid);
    }
    
    public Set<String> getFakeClans() {
        return new HashSet<>(fakeClans);
    }
    
    public void createFakeClan(String name, UUID leader) {
        synchronized (lock) {
            fakeClans.add(name);
            klanLeaders.put(name, leader.toString());
            klanLevels.put(name, 1);
            klanMemberLimits.put(name, 1);
            klanBankLimits.put(name, 1);
            klanBankCache.put(name, 0.0);
            klanElo.put(name, 1000);
            klanLeague.put(name, "Bronze");
            roleCache.put(leader.toString(), com.dkprojects.dkklan.objects.ClanRole.LEADER);
            playerKlan.put(leader.toString(), name);
            List<String> members = klanMembers.computeIfAbsent(name, k -> new ArrayList<>());
            if (!members.contains(leader.toString())) members.add(leader.toString());
        }
    }
    
    public void addFakeBots(String clan, int count) {
        synchronized (lock) {
            if (!fakeClans.contains(clan)) return;
            List<String> members = klanMembers.computeIfAbsent(clan, k -> new ArrayList<>());
            for (int i = 0; i < count; i++) {
                UUID bot = UUID.randomUUID();
                String id = bot.toString();
                playerKlan.put(id, clan);
                roleCache.put(id, com.dkprojects.dkklan.objects.ClanRole.MEMBER);
                members.add(id);
            }
        }
    }
    public void createKlan(String name, UUID leader) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try {
                    try (PreparedStatement ps = connection.prepareStatement("INSERT INTO klans (name, leader, elo, league) VALUES (?, ?, 1000, 'Bronze')")) {
                        ps.setString(1, name);
                        ps.setString(2, leader.toString());
                        ps.executeUpdate();
                    }
                    
                    try (PreparedStatement ps = connection.prepareStatement("INSERT INTO klan_members (uuid, klan_name, role) VALUES (?, ?, 'LEADER')")) {
                        ps.setString(1, leader.toString());
                        ps.setString(2, name);
                        ps.executeUpdate();
                    }
                    
                    playerKlan.put(leader.toString(), name);
                    klanMembers.put(name, new ArrayList<>(Collections.singletonList(leader.toString())));
                    klanLeaders.put(name, leader.toString());
                    klanLevels.put(name, 1);
                    klanMemberLimits.put(name, 1);
                    klanBankLimits.put(name, 1);
                    klanBankCache.put(name, 0.0);
                    klanElo.put(name, 1000);
                    klanLeague.put(name, "Bronze");
                    roleCache.put(leader.toString(), ClanRole.LEADER);
                    
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }
    
    public void deleteKlan(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try {
                    try (PreparedStatement ps = connection.prepareStatement("DELETE FROM klans WHERE name = ?")) {
                        ps.setString(1, name);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = connection.prepareStatement("DELETE FROM klan_members WHERE klan_name = ?")) {
                        ps.setString(1, name);
                        ps.executeUpdate();
                    }
                    
                    List<String> members = klanMembers.remove(name);
                    if (members != null) {
                        for (String uuid : members) {
                            playerKlan.remove(uuid);
                            roleCache.remove(uuid);
                            clanChatMode.remove(UUID.fromString(uuid));
                        }
                    }
                    klanBankCache.remove(name);
                    klanLeaders.remove(name);
                    klanLevels.remove(name);
                    klanMemberLimits.remove(name);
                    klanBankLimits.remove(name);
                    klanBases.remove(name);
                    klanElo.remove(name);
                    klanLeague.remove(name);

                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    public ClanRole getRole(UUID uuid) {
        if (roleCache.containsKey(uuid.toString())) {
            return roleCache.get(uuid.toString());
        }
        
        String klan = getPlayerKlan(uuid);
        if (klan != null && isLeader(uuid)) {
            roleCache.put(uuid.toString(), ClanRole.LEADER);
            return ClanRole.LEADER;
        }

        ClanRole role = ClanRole.MEMBER;
        synchronized (lock) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT role FROM klan_members WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        role = ClanRole.fromString(rs.getString("role"));
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
        roleCache.put(uuid.toString(), role);
        return role;
    }

    public void setRole(UUID uuid, ClanRole role) {
        roleCache.put(uuid.toString(), role);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("UPDATE klan_members SET role = ? WHERE uuid = ?")) {
                    ps.setString(1, role.name());
                    ps.setString(2, uuid.toString());
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    public void promote(UUID target, UUID actor) {
        String klan = getPlayerKlan(target);
        if (klan == null) return;
        
        ClanRole currentRole = getRole(target);
        ClanRole nextRole = currentRole.getNext();
        
        if (nextRole == null) {
            if (currentRole == ClanRole.ADMIN) {
                 if (!isLeader(actor)) return;
                 setRole(target, ClanRole.LEADER);
                 setRole(actor, ClanRole.ADMIN);
                 klanLeaders.put(klan, target.toString());
                 Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    synchronized (lock) {
                        try (PreparedStatement ps = connection.prepareStatement("UPDATE klans SET leader = ? WHERE name = ?")) {
                            ps.setString(1, target.toString());
                            ps.setString(2, klan);
                            ps.executeUpdate();
                        } catch (SQLException e) { e.printStackTrace(); }
                    }
                 });
            }
            return;
        }
        setRole(target, nextRole);
    }

    public String getPlayerKlan(UUID uuid) { return playerKlan.get(uuid.toString()); }
    public List<String> getMembers(String name) { return klanMembers.getOrDefault(name, new ArrayList<>()); }
    public Set<String> getAllClans() { return klanMembers.keySet(); }
    public String getPendingInvite(UUID uuid) { return pendingInvites.get(uuid); }
    public String getKlanLeader(String name) { return klanLeaders.get(name); }
    public boolean isLeader(UUID uuid) {
        String klan = getPlayerKlan(uuid);
        if (klan == null) return false;
        String leader = getKlanLeader(klan);
        return leader != null && leader.equals(uuid.toString());
    }
    
    public int getKlanLevel(String name) { return klanLevels.getOrDefault(name, 1); }
    public double getKlanBank(String name) { return klanBankCache.getOrDefault(name, 0.0); }
    public int getMemberLimitLevel(String name) { return klanMemberLimits.getOrDefault(name, 1); }
    public int getBankLimitLevel(String name) { return klanBankLimits.getOrDefault(name, 1); }
    public int getKlanElo(String name) { return klanElo.getOrDefault(name, 1000); }
    public String getKlanLeague(String name) { return klanLeague.getOrDefault(name, "Bronze"); }
    public Location getKlanBase(String name) { return klanBases.get(name); }
    public boolean isAssistant(UUID uuid) { return getRole(uuid).isAtLeast(ClanRole.ADMIN); }
    
    public void incrementMemberLimitLevel(String name) {
        int val = getMemberLimitLevel(name) + 1;
        klanMemberLimits.put(name, val);
        updateInt(name, "member_limit_level", val);
    }
    public void incrementBankLimitLevel(String name) {
        int val = getBankLimitLevel(name) + 1;
        klanBankLimits.put(name, val);
        updateInt(name, "bank_limit_level", val);
    }
    public double getNextMemberLimitCost(int level) { return level * 2500; }
    public double getNextBankLimitCost(int level) { return level * 5000; }
    
    public void deposit(String name, double amount) {
        double val = getKlanBank(name) + amount;
        klanBankCache.put(name, val);
        updateDouble(name, "bank", val);
    }
    public void withdraw(String name, double amount) {
        double val = getKlanBank(name) - amount;
        klanBankCache.put(name, val);
        updateDouble(name, "bank", val);
    }

    public double getBankBalance(String name) { return getKlanBank(name); }
    public void depositBank(String name, double amount) { deposit(name, amount); }
    public void withdrawBank(String name, double amount) { withdraw(name, amount); }
    
    public java.util.List<UUID> getOnlineMembers(String clan) {
        java.util.List<UUID> list = new java.util.ArrayList<>();
        for (String id : getMembers(clan)) {
            try {
                UUID u = UUID.fromString(id);
                org.bukkit.entity.Player p = Bukkit.getPlayer(u);
                if (p != null && p.isOnline()) list.add(u);
            } catch (Exception ignored) {}
        }
        return list;
    }
    
    public Location getSpawnLocation() {
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();
    }
    
    public void setKlanBase(String name, Location loc) {
        klanBases.put(name, loc);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("UPDATE klans SET base = ? WHERE name = ?")) {
                    ps.setString(1, serializeLocation(loc));
                    ps.setString(2, name);
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }
    
    public boolean toggleClanChat(UUID uuid) {
        if (clanChatMode.contains(uuid)) {
            clanChatMode.remove(uuid);
            return false;
        } else {
            clanChatMode.add(uuid);
            return true;
        }
    }
    public boolean isInClanChat(UUID uuid) { return clanChatMode.contains(uuid); }

    private void updateInt(String name, String col, int val) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("UPDATE klans SET " + col + " = ? WHERE name = ?")) {
                    ps.setInt(1, val);
                    ps.setString(2, name);
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }
    private void updateDouble(String name, String col, double val) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("UPDATE klans SET " + col + " = ? WHERE name = ?")) {
                    ps.setDouble(1, val);
                    ps.setString(2, name);
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    public void broadcastToClan(String klanName, String message) {
        List<String> members = getMembers(klanName);
        for (String uuidStr : members) {
            Player p = Bukkit.getPlayer(UUID.fromString(uuidStr));
            if (p != null && p.isOnline()) {
                p.sendMessage(message);
            }
        }
    }

    public void leaveClan(UUID player) {
        String klan = getPlayerKlan(player);
        if (klan == null) return;
        
        if (isLeader(player)) {
            // Leader cannot leave, must disband
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM klan_members WHERE uuid = ?")) {
                    ps.setString(1, player.toString());
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });

        playerKlan.remove(player.toString());
        roleCache.remove(player.toString());
        clanChatMode.remove(player);
        if (klanMembers.containsKey(klan)) {
            klanMembers.get(klan).remove(player.toString());
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            p.sendMessage("§8[§6MaveraKlan§8] §c" + klan + " klanından ayrıldınız.");
        }
        
        broadcastToClan(klan, "§e" + Bukkit.getOfflinePlayer(player).getName() + " klanından ayrıldı.");
    }

    public void kickMember(UUID target, UUID actor) {
        String klan = getPlayerKlan(target);
        if (klan == null || !isLeader(actor)) return;
        
        if (target.equals(actor)) {
             return; 
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM klan_members WHERE uuid = ?")) {
                    ps.setString(1, target.toString());
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });

        playerKlan.remove(target.toString());
        roleCache.remove(target.toString());
        clanChatMode.remove(target);
        if (klanMembers.containsKey(klan)) {
            klanMembers.get(klan).remove(target.toString());
        }

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§8[§6MaveraKlan§8] §c" + klan + " klanından atıldınız!");
        }
        
        Player leader = Bukkit.getPlayer(actor);
        if (leader != null) {
            leader.sendMessage("§8[§6MaveraKlan§8] §a" + Bukkit.getOfflinePlayer(target).getName() + " klanından atıldı!");
        }
        
        broadcastToClan(klan, "§e" + Bukkit.getOfflinePlayer(target).getName() + " klanından atıldı!");
    }

    // Missing methods implementation
    public void loadKlanData() { loadCache(); }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void invitePlayer(UUID inviter, UUID target) {
        String klan = getPlayerKlan(inviter);
        if (klan == null) return;
        pendingInvites.put(target, klan);
        
        // Auto expire after 60s
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingInvites.containsKey(target) && pendingInvites.get(target).equals(klan)) {
                pendingInvites.remove(target);
                Player p = Bukkit.getPlayer(target);
                if (p != null) p.sendMessage(plugin.getMessage("invite-expired-msg").replace("%clan%", klan));
            }
        }, 1200L);
    }

    public void acceptInvite(UUID player) {
        if (!pendingInvites.containsKey(player)) {
            Player p = Bukkit.getPlayer(player);
            if (p != null) p.sendMessage("§8[§6MaveraKlan§8] §cBekleyen davetiniz yok!");
            return;
        }
        
        String klan = pendingInvites.remove(player);
        if (getMembers(klan).size() >= calculateMemberLimit(getMemberLimitLevel(klan))) {
            Player p = Bukkit.getPlayer(player);
            if (p != null) p.sendMessage("§8[§6MaveraKlan§8] §cKlan üye limiti dolu!");
            return;
        }

        addMember(klan, player);
        Player p = Bukkit.getPlayer(player);
        if (p != null) p.sendMessage(plugin.getMessage("join-success-msg").replace("%clan%", klan));
        broadcastToClan(klan, plugin.getMessage("join-broadcast-msg").replace("%player%", Bukkit.getOfflinePlayer(player).getName()));
    }

    public void addMember(String klan, UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("INSERT INTO klan_members (uuid, klan_name, role) VALUES (?, ?, 'MEMBER')")) {
                    ps.setString(1, player.toString());
                    ps.setString(2, klan);
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
        
        playerKlan.put(player.toString(), klan);
        if (klanMembers.containsKey(klan)) klanMembers.get(klan).add(player.toString());
        roleCache.put(player.toString(), ClanRole.MEMBER);
    }

    public int calculateMemberLimit(int level) { return level * 5 + 5; } // Example logic
    public int calculateBankLimit(int level) { return level * 50000; } // Example logic
    
    public List<String> getKlanMembers(String name) { return getMembers(name); }

    public void saveWarHistory(String clanA, String clanB, String winner, int scoreA, int scoreB) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("INSERT INTO clan_wars (clan_a, clan_b, winner, score_a, score_b, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, clanA);
                    ps.setString(2, clanB);
                    ps.setString(3, winner);
                    ps.setInt(4, scoreA);
                    ps.setInt(5, scoreB);
                    ps.setLong(6, System.currentTimeMillis()); // Approx start
                    ps.setLong(7, System.currentTimeMillis()); // End
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    public int getRecentMatchCount(String clanName, String opponent, long timeWindow) {
        // Simplified check, could query DB
        return 0; 
    }

    public void setKlanElo(String name, int elo) {
        klanElo.put(name, elo);
        updateInt(name, "elo", elo);
    }

    public void setKlanLeague(String name, String league) {
        klanLeague.put(name, league);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                try (PreparedStatement ps = connection.prepareStatement("UPDATE klans SET league = ? WHERE name = ?")) {
                    ps.setString(1, league);
                    ps.setString(2, name);
                    ps.executeUpdate();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private String serializeLocation(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
    }
    private Location deserializeLocation(String s) {
        if (s == null) return null;
        String[] p = s.split(":");
        World w = Bukkit.getWorld(p[0]);
        if (w == null) return null;
        return new Location(w, Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]), Float.parseFloat(p[4]), Float.parseFloat(p[5]));
    }
}

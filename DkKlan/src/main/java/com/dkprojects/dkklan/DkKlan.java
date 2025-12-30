package com.dkprojects.dkklan;

import org.bukkit.plugin.java.JavaPlugin;
import com.dkprojects.dkklan.managers.KlanManager;
import com.dkprojects.dkklan.managers.ConfigManager;
import com.dkprojects.dkklan.commands.KlanCommand;
import com.dkprojects.dkklan.listeners.KlanListener;
import com.dkprojects.dkklan.listeners.MenuListener;
import com.dkprojects.dkklan.hooks.KlanPlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.dkprojects.dkklan.managers.WarManager;
import com.dkprojects.dkklan.managers.EloManager;
import com.dkprojects.dkklan.managers.HologramManager;
import com.dkprojects.dkklan.managers.SeasonManager;
import com.dkprojects.dkklan.managers.RulesetManager;
import com.dkprojects.dkklan.managers.DiscordManager;
import com.dkprojects.dkklan.managers.TournamentManager;
import com.dkprojects.dkklan.managers.KitManager;
import com.dkprojects.dkklan.listeners.WarListener;

public class DkKlan extends JavaPlugin {
    private static DkKlan instance;
    private KlanManager klanManager;
    private WarManager warManager;
    private EloManager eloManager;
    private HologramManager hologramManager;
    private SeasonManager seasonManager;
    private RulesetManager rulesetManager;
    private DiscordManager discordManager;
    private TournamentManager tournamentManager;
    private KitManager kitManager;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        try {
            instance = this;
            
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            // Smart Config Update
            new ConfigManager(this).updateConfig("config.yml");
            reloadConfig();

            if (!setupEconomy()) {
                getLogger().warning("Vault bulunamadı; ekonomi özellikleri devre dışı.");
            }

            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            klanManager = new KlanManager(this);
            // klanManager.loadKlanData(); // Removed as it is handled in constructor
            
            // Initialize new managers
            eloManager = new EloManager(this);
            hologramManager = new HologramManager(this);
            seasonManager = new SeasonManager(this);
            rulesetManager = new RulesetManager(this);
            discordManager = new DiscordManager(this);
            tournamentManager = new TournamentManager(this);
            kitManager = new KitManager(this);
            warManager = new WarManager(this);
            
            KlanCommand cmd = new KlanCommand(this);
            getCommand("klan").setExecutor(cmd);
            getCommand("klan").setTabCompleter(cmd);
            getCommand("k").setExecutor(cmd);
            getCommand("k").setTabCompleter(cmd);
            com.dkprojects.dkklan.commands.WarCommand wc = new com.dkprojects.dkklan.commands.WarCommand(this);
            getCommand("klansavasi").setExecutor(wc);
            getCommand("klansavasi").setTabCompleter(wc);
            com.dkprojects.dkklan.commands.DkKlanAdminCommand ac = new com.dkprojects.dkklan.commands.DkKlanAdminCommand(this);
            getCommand("dkklan").setExecutor(ac);
            getCommand("dkklan").setTabCompleter(ac);
            getServer().getPluginManager().registerEvents(new KlanListener(this), this);
            getServer().getPluginManager().registerEvents(new MenuListener(this), this);
            getServer().getPluginManager().registerEvents(new WarListener(this), this);

            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new KlanPlaceholderExpansion(this).register();
            }

            getLogger().info("DkKlan aktif! Mavera sunucusu icin hazir.");
        } catch (Exception e) {
            getLogger().severe("DkKlan baslatilirken kritik bir hata olustu!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public String getMessage(String key) {
        String msg = getConfig().getString("messages." + key);
        if (msg == null) return "Message not found: messages." + key;
        String prefix = getConfig().getString("messages.prefix", "");
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public String getRawMessage(String key) {
        String msg = getConfig().getString("messages." + key);
        if (msg == null) return key;
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }

    public java.util.List<String> getStringList(String key) {
        if (!getConfig().isList("messages." + key)) return java.util.Collections.emptyList();
        java.util.List<String> list = getConfig().getStringList("messages." + key);
        java.util.List<String> colored = new java.util.ArrayList<>();
        for (String s : list) {
            colored.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', s));
        }
        return colored;
    }

    @Override
    public void onDisable() {
        if (warManager != null) {
            warManager.cancelAllWars();
        }
        if (klanManager != null) {
            klanManager.close();
        }
        getLogger().info("DkKlan deaktif!");
    }

    public static DkKlan getInstance() { return instance; }
 public KlanManager getKlanManager() { return klanManager; }
    public WarManager getWarManager() { return warManager; }
    public EloManager getEloManager() { return eloManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public SeasonManager getSeasonManager() { return seasonManager; }
    public RulesetManager getRulesetManager() { return rulesetManager; }
    public DiscordManager getDiscordManager() { return discordManager; }
    public TournamentManager getTournamentManager() { return tournamentManager; }
    public KitManager getKitManager() { return kitManager; }
}

package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.WarRuleset;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class RulesetManager {
    private final DkKlan plugin;
    private final Map<String, WarRuleset> rulesets = new HashMap<>();
    private final File rulesFile;
    private FileConfiguration rulesConfig;

    public RulesetManager(DkKlan plugin) {
        this.plugin = plugin;
        this.rulesFile = new File(plugin.getDataFolder(), "rulesets.yml");
        loadRulesets();
    }

    private void loadRulesets() {
        if (!rulesFile.exists()) {
            createDefaultRulesets();
            return;
        }
        
        rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
        for (String key : rulesConfig.getConfigurationSection("rulesets").getKeys(false)) {
            String path = "rulesets." + key;
            WarRuleset ruleset = new WarRuleset(key);
            
            ruleset.setAllowCobweb(rulesConfig.getBoolean(path + ".allow_cobweb", true));
            ruleset.setAllowEnderPearl(rulesConfig.getBoolean(path + ".allow_ender_pearl", true));
            ruleset.setAllowElytra(rulesConfig.getBoolean(path + ".allow_elytra", false));
            ruleset.setKeepInventory(rulesConfig.getBoolean(path + ".keep_inventory", true));
            ruleset.setFriendlyFire(rulesConfig.getBoolean(path + ".friendly_fire", false));
            ruleset.setBetAllowed(rulesConfig.getBoolean(path + ".bet_allowed", true));
            ruleset.setMaxPlayers(rulesConfig.getInt(path + ".max_players", 10));
            
            ruleset.setKitMode(rulesConfig.getBoolean(path + ".kit_mode", true));
            ruleset.setAllowSub(rulesConfig.getBoolean(path + ".allow_sub", false));
            ruleset.setInventoryLock(rulesConfig.getBoolean(path + ".inventory_lock", true));
            
            List<String> blacklist = rulesConfig.getStringList(path + ".blacklisted_items");
            ruleset.setBlacklistedItems(blacklist);
            
            rulesets.put(key, ruleset);
        }
    }

    private void createDefaultRulesets() {
        // Normal Ruleset
        WarRuleset normal = new WarRuleset("Normal");
        normal.setKitMode(true);
        normal.setInventoryLock(true);
        rulesets.put("Normal", normal);
        
        // Sponsor Ruleset
        WarRuleset sponsor = new WarRuleset("Sponsor2025");
        sponsor.setAllowCobweb(true);
        sponsor.setAllowEnderPearl(true);
        sponsor.setAllowElytra(false);
        sponsor.setBetAllowed(false);
        sponsor.setMaxPlayers(5);
        sponsor.setKitMode(true);
        sponsor.setInventoryLock(true);
        rulesets.put("Sponsor2025", sponsor);
        
        saveRulesets();
    }

    public void saveRulesets() {
        rulesConfig = new YamlConfiguration();
        for (WarRuleset ruleset : rulesets.values()) {
            String path = "rulesets." + ruleset.getName();
            rulesConfig.set(path + ".allow_cobweb", ruleset.isAllowCobweb());
            rulesConfig.set(path + ".allow_ender_pearl", ruleset.isAllowEnderPearl());
            rulesConfig.set(path + ".allow_elytra", ruleset.isAllowElytra());
            rulesConfig.set(path + ".keep_inventory", ruleset.isKeepInventory());
            rulesConfig.set(path + ".friendly_fire", ruleset.isFriendlyFire());
            rulesConfig.set(path + ".bet_allowed", ruleset.isBetAllowed());
            rulesConfig.set(path + ".max_players", ruleset.getMaxPlayers());
            rulesConfig.set(path + ".kit_mode", ruleset.isKitMode());
            rulesConfig.set(path + ".allow_sub", ruleset.isAllowSub());
            rulesConfig.set(path + ".inventory_lock", ruleset.isInventoryLock());
            rulesConfig.set(path + ".blacklisted_items", ruleset.getBlacklistedItems());
        }
        
        try {
            rulesConfig.save(rulesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public WarRuleset getRuleset(String name) {
        return rulesets.get(name);
    }
    
    private String activeRulesetName = "Normal";

    public WarRuleset getDefaultRuleset() {
        return rulesets.getOrDefault(activeRulesetName, rulesets.get("Normal"));
    }
    
    public void setActiveRuleset(String name) {
        if (rulesets.containsKey(name)) {
            this.activeRulesetName = name;
        }
    }
    
    public String getActiveRulesetName() {
        return activeRulesetName;
    }
    
    public void createRuleset(String name) {
        if (!rulesets.containsKey(name)) {
            rulesets.put(name, new WarRuleset(name));
            saveRulesets();
        }
    }
    
    public java.util.Set<String> getRulesetNames() {
        return rulesets.keySet();
    }
}

package com.dkprojects.dkklan.objects;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public class WarRuleset {
    private String name;
    private boolean allowCobweb;
    private boolean allowEnderPearl;
    private boolean allowElytra;
    private boolean keepInventory;
    private boolean friendlyFire;
    private boolean betAllowed;
    private int maxPlayers;
    private boolean kitMode;
    private boolean allowSub;
    private boolean inventoryLock;
    private List<String> blacklistedItems;

    public WarRuleset(String name) {
        this.name = name;
        this.allowCobweb = true;
        this.allowEnderPearl = true;
        this.allowElytra = false;
        this.keepInventory = true;
        this.friendlyFire = false;
        this.betAllowed = true;
        this.maxPlayers = 10;
        this.kitMode = true; // Default to true as per new requirement
        this.allowSub = false; // Default to false
        this.inventoryLock = true; // Default to true
        this.blacklistedItems = new ArrayList<>();
    }

    public String getName() { return name; }
    
    public boolean isAllowCobweb() { return allowCobweb; }
    public void setAllowCobweb(boolean allowCobweb) { this.allowCobweb = allowCobweb; }
    
    public boolean isAllowEnderPearl() { return allowEnderPearl; }
    public void setAllowEnderPearl(boolean allowEnderPearl) { this.allowEnderPearl = allowEnderPearl; }
    
    public boolean isAllowElytra() { return allowElytra; }
    public void setAllowElytra(boolean allowElytra) { this.allowElytra = allowElytra; }
    
    public boolean isKeepInventory() { return keepInventory; }
    public void setKeepInventory(boolean keepInventory) { this.keepInventory = keepInventory; }
    
    public boolean isFriendlyFire() { return friendlyFire; }
    public void setFriendlyFire(boolean friendlyFire) { this.friendlyFire = friendlyFire; }
    
    public boolean isBetAllowed() { return betAllowed; }
    public void setBetAllowed(boolean betAllowed) { this.betAllowed = betAllowed; }
    
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    
    public boolean isKitMode() { return kitMode; }
    public void setKitMode(boolean kitMode) { this.kitMode = kitMode; }

    public boolean isAllowSub() { return allowSub; }
    public void setAllowSub(boolean allowSub) { this.allowSub = allowSub; }

    public boolean isInventoryLock() { return inventoryLock; }
    public void setInventoryLock(boolean inventoryLock) { this.inventoryLock = inventoryLock; }

    public List<String> getBlacklistedItems() { return blacklistedItems; }
    public void setBlacklistedItems(List<String> blacklistedItems) { this.blacklistedItems = blacklistedItems; }
    
    public boolean isItemAllowed(Material mat) {
        if (mat == Material.ELYTRA && !allowElytra) return false;
        if (mat == Material.ENDER_PEARL && !allowEnderPearl) return false;
        if (mat == Material.COBWEB && !allowCobweb) return false;
        return !blacklistedItems.contains(mat.name());
    }
}

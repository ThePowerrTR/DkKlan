package com.dkprojects.dkklan.objects;

import com.dkprojects.dkklan.war.ArenaSize;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class WarArena {
    private final String name;
    private Location pos1;
    private Location pos2;
    private Location lobby;
    private Location spawn1;
    private Location spawn2;
    private boolean inUse;
    
    private ArenaSize size = ArenaSize.MEDIUM;
    private int minPlayers = 2;
    private int maxPlayers = 20;
    
    private java.util.Map<String, Location> cameraPoints = new java.util.HashMap<>();

    public WarArena(String name, Location pos1, Location pos2) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.inUse = false;
        calculateSize();
    }
    
    private void calculateSize() {
        if (pos1 == null || pos2 == null) return;
        double dist = pos1.distance(pos2);
        if (dist < 50) size = ArenaSize.SMALL;
        else if (dist < 100) size = ArenaSize.MEDIUM;
        else size = ArenaSize.LARGE;
    }

    public ArenaSize getSize() { return size; }
    public void setSize(ArenaSize size) { this.size = size; }
    
    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int min) { this.minPlayers = min; }
    
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int max) { this.maxPlayers = max; }

    public String getName() {
        return name;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getLobby() {
        return lobby;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public Location getSpawn1() {
        return spawn1;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    public Location getSpawn2() {
        return spawn2;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }
    
    public boolean isInUse() {
        return inUse;
    }
    
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
    
    public boolean isSetup() {
        return pos1 != null && pos2 != null && lobby != null && spawn1 != null && spawn2 != null;
    }
    
    public boolean isInside(Location loc) {
        if (pos1 == null || pos2 == null) return false;
        if (!loc.getWorld().equals(pos1.getWorld())) return false;
        
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public void addCameraPoint(String name, Location loc) { cameraPoints.put(name, loc); }
    public Location getCameraPoint(String name) { return cameraPoints.get(name); }
    public java.util.Map<String, Location> getCameraPoints() { return cameraPoints; }
    public void removeCameraPoint(String name) { cameraPoints.remove(name); }
}

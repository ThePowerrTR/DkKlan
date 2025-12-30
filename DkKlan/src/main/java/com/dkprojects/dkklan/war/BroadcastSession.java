package com.dkprojects.dkklan.war;

import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;

public class BroadcastSession {
    private final MatchSeries series;
    private final Set<UUID> casters = new HashSet<>();
    
    public BroadcastSession(MatchSeries series) {
        this.series = series;
    }
    
    public MatchSeries getSeries() { return series; }
    
    public void addCaster(Player p) {
        if (casters.contains(p.getUniqueId())) return;
        casters.add(p.getUniqueId());
        
        // Caster Settings
        p.setGameMode(GameMode.SPECTATOR);
        p.sendMessage("§aCaster moduna geçtiniz!");
        p.sendMessage("§eKamera noktaları için: /klansavasi camera tp <isim>");
    }
    
    public void removeCaster(Player p) {
        if (!casters.contains(p.getUniqueId())) return;
        casters.remove(p.getUniqueId());
        
        p.setGameMode(GameMode.SURVIVAL);
        p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        p.sendMessage("§cCaster modundan çıktınız.");
    }
    
    public boolean isCaster(Player p) {
        return casters.contains(p.getUniqueId());
    }
    
    public Set<UUID> getCasters() { return casters; }
    
    public void stop() {
        for (UUID uuid : new HashSet<>(casters)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) removeCaster(p);
        }
    }
}

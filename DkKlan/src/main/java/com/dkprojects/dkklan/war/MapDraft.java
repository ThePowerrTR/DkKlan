package com.dkprojects.dkklan.war;

import com.dkprojects.dkklan.objects.WarArena;
import org.bukkit.Bukkit;

import java.util.*;

public class MapDraft {
    private final String clanA;
    private final String clanB;
    private final List<WarArena> pool;
    private final List<WarArena> bannedMaps = new ArrayList<>();
    private final Queue<WarArena> roundOrder = new LinkedList<>();
    
    private String currentTurn; // Clan name whose turn it is
    private boolean isFinished = false;
    private int bansNeeded = 2; // 1 per clan
    private int bansDone = 0;

    public MapDraft(String clanA, String clanB, List<WarArena> pool) {
        this.clanA = clanA;
        this.clanB = clanB;
        this.pool = new ArrayList<>(pool);
        this.currentTurn = clanA; // Clan A starts
    }
    
    public boolean banMap(String clan, String mapName) {
        if (isFinished) return false;
        if (!clan.equals(currentTurn)) return false;
        
        WarArena target = null;
        for (WarArena a : pool) {
            if (a.getName().equalsIgnoreCase(mapName)) {
                target = a;
                break;
            }
        }
        
        if (target == null) return false;
        
        pool.remove(target);
        bannedMaps.add(target);
        bansDone++;
        
        if (bansDone >= bansNeeded) {
            finishDraft();
        } else {
            // Switch turn
            currentTurn = currentTurn.equals(clanA) ? clanB : clanA;
            notifyTurn();
        }
        
        return true;
    }
    
    private void finishDraft() {
        isFinished = true;
        currentTurn = null;
        
        // Shuffle remaining maps and add to queue
        Collections.shuffle(pool);
        roundOrder.addAll(pool);
    }
    
    public void forceFinish() {
        if (isFinished) return;
        isFinished = true;
        currentTurn = null;
        Collections.shuffle(pool);
        roundOrder.clear();
        roundOrder.addAll(pool);
    }
    
    public void notifyTurn() {
        Bukkit.broadcastMessage("§e[Turnuva] §7Sıra §6" + currentTurn + " §7klanında! Bir harita yasaklayın.");
        Bukkit.broadcastMessage("§e/klansavasi map ban <isim>");
        Bukkit.broadcastMessage("§7Kalan Haritalar: " + getMapNames());
    }
    
    public String getMapNames() {
        StringBuilder sb = new StringBuilder();
        for (WarArena a : pool) {
            sb.append(a.getName()).append(", ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }
    
    public WarArena nextMap() {
        if (roundOrder.isEmpty()) {
            // If we run out of maps, recycle the banned ones or reshuffle existing?
            // User says "Kalan mapler -> round sırasına dizilir"
            // If BO5 and only 2 maps left?
            // Let's recycle the pool (which now contains only allowed maps)
            if (pool.isEmpty()) return null; // Should not happen if we have maps
            Collections.shuffle(pool);
            roundOrder.addAll(pool);
        }
        return roundOrder.poll();
    }

    public boolean isFinished() {
        return isFinished;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public List<WarArena> getPool() {
        return pool;
    }

    public List<WarArena> getBannedMaps() {
        return bannedMaps;
    }
}

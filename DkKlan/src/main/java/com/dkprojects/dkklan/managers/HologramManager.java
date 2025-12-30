package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {
    private final DkKlan plugin;
    private final Map<String, Hologram> activeHolograms = new HashMap<>();

    public HologramManager(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void createWarHologram(String warId, Location location, String clanA, String clanB, int bet) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add(plugin.getRawMessage("hologram-title"));
            lines.add(plugin.getRawMessage("hologram-versus")
                    .replace("%clanA%", clanA)
                    .replace("%clanB%", clanB));
            lines.add(plugin.getRawMessage("hologram-bet").replace("%bet%", String.valueOf(bet)));
            lines.add(plugin.getRawMessage("hologram-starting"));

            Hologram hologram = DHAPI.createHologram(warId, location, lines);
            activeHolograms.put(warId, hologram);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWarHologram(String warId, int scoreA, int scoreB, long timeRemaining) {
        Hologram hologram = activeHolograms.get(warId);
        if (hologram != null) {
            // Update score line (index 1) - wait, index 1 is "clanA vs clanB". 
            // Let's just update the status line (index 3) for now.
            DHAPI.setHologramLine(hologram, 3, plugin.getRawMessage("hologram-status")
                    .replace("%scoreA%", String.valueOf(scoreA))
                    .replace("%scoreB%", String.valueOf(scoreB))
                    .replace("%time%", String.valueOf(timeRemaining)));
        }
    }

    public void removeWarHologram(String warId) {
        Hologram hologram = activeHolograms.remove(warId);
        if (hologram != null) {
            try {
                DHAPI.removeHologram(warId);
            } catch (Exception e) {
                // Already removed or error
            }
        }
    }
    
    public void removeAllHolograms() {
        for (String id : new ArrayList<>(activeHolograms.keySet())) {
            removeWarHologram(id);
        }
    }
}

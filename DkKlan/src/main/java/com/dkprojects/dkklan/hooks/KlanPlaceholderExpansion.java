package com.dkprojects.dkklan.hooks;

import com.dkprojects.dkklan.DkKlan;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class KlanPlaceholderExpansion extends PlaceholderExpansion {

    private final DkKlan plugin;

    public KlanPlaceholderExpansion(DkKlan plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dkklan";
    }

    @Override
    public @NotNull String getAuthor() {
        return "DKProjects";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("name")) {
            String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
            return klanName != null ? klanName : "Yok";
        }
        
        if (params.equalsIgnoreCase("name_formatted")) {
            String klanName = plugin.getKlanManager().getPlayerKlan(player.getUniqueId());
            return klanName != null ? "&e[" + klanName + "]" : "";
        }

        return null;
    }
}

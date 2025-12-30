package com.dkprojects.dkklan.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {
    public enum Key {
        MENU_OPEN,
        SELECT_SUCCESS,
        SUCCESS,
        LOCK_ERROR,
        ACCEPT,
        WAR_START,
        MAP_BAN,
        TURN_PLING
    }
    public static void play(Player p, Key key) {
        if (p == null) return;
        switch (key) {
            case MENU_OPEN:
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                break;
            case SELECT_SUCCESS:
            case SUCCESS:
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
                break;
            case LOCK_ERROR:
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.8f);
                break;
            case ACCEPT:
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                break;
            case WAR_START:
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 0.8f);
                break;
            case MAP_BAN:
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.6f, 1f);
                break;
            case TURN_PLING:
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                break;
        }
    }
}

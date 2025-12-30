package com.dkprojects.dkklan.objects;

public enum ClanRole {
    LEADER(4, "§cKlan Sahibi"),
    ADMIN(3, "§6Yönetici"),
    MODERATOR(2, "§eModeratör"),
    MEMBER(1, "§7Üye");

    private final int weight;
    private final String displayName;

    ClanRole(int weight, String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }

    public int getWeight() {
        return weight;
    }

    public int getLevel() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAtLeast(ClanRole role) {
        return this.weight >= role.weight;
    }
    
    public ClanRole getNext() {
        if (this == MEMBER) return MODERATOR;
        if (this == MODERATOR) return ADMIN;
        return null; // Cannot promote further via simple click (Leader needs transfer)
    }

    public static ClanRole fromString(String role) {
        try {
            return valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }
}

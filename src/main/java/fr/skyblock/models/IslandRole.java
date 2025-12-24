package fr.skyblock.models;

public enum IslandRole {
    OWNER("Propriétaire", 4),
    CO_OWNER("Co-Propriétaire", 3),
    MEMBER("Membre", 2),
    VISITOR("Visiteur", 1);
    
    private final String displayName;
    private final int priority;
    
    IslandRole(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean canManage(IslandRole otherRole) {
        return this.priority > otherRole.priority;
    }
}
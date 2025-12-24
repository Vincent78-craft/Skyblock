package fr.skyblock.models;

public enum MissionType {
    BREAK_BLOCKS("Casser des blocs"),
    KILL_MOBS("Tuer des mobs"),
    COLLECT_ITEMS("Collecter des objets"),
    SPEND_COINS("Dépenser des SkyCoins"),
    BUILD_BLOCKS("Poser des blocs"),
    CRAFT_ITEMS("Crafter des objets"),
    VISIT_ISLANDS("Visiter des îles");
    
    private final String displayName;
    
    MissionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
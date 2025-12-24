package fr.skyblock.models;

import java.util.*;

public class SkyPlayer {
    
    private final UUID uuid;
    private String name;
    private String currentIslandId;
    private int skyCoins;
    private long lastLogin;
    
    private final Map<String, Integer> completedMissions;
    private final Map<String, Object> playerData;
    
    public SkyPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.currentIslandId = null;
        this.skyCoins = 0;
        this.lastLogin = System.currentTimeMillis();
        this.completedMissions = new HashMap<>();
        this.playerData = new HashMap<>();
    }
    
    public void addSkyCoins(int amount) {
        this.skyCoins += amount;
    }
    
    public boolean removeSkyCoins(int amount) {
        if (this.skyCoins >= amount) {
            this.skyCoins -= amount;
            return true;
        }
        return false;
    }
    
    public void completeMission(String missionId) {
        completedMissions.put(missionId, completedMissions.getOrDefault(missionId, 0) + 1);
    }
    
    public int getMissionCompletions(String missionId) {
        return completedMissions.getOrDefault(missionId, 0);
    }
    
    public boolean hasMission(String missionId) {
        return completedMissions.containsKey(missionId);
    }
    
    public void setPlayerData(String key, Object value) {
        playerData.put(key, value);
    }
    
    public Object getPlayerData(String key) {
        return playerData.get(key);
    }
    
    public Object getPlayerData(String key, Object defaultValue) {
        return playerData.getOrDefault(key, defaultValue);
    }
    
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }
    
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrentIslandId() { return currentIslandId; }
    public void setCurrentIslandId(String currentIslandId) { this.currentIslandId = currentIslandId; }
    public int getSkyCoins() { return skyCoins; }
    public void setSkyCoins(int skyCoins) { this.skyCoins = skyCoins; }
    public long getLastLogin() { return lastLogin; }
    public Map<String, Integer> getCompletedMissions() { return new HashMap<>(completedMissions); }
    public Map<String, Object> getPlayerData() { return new HashMap<>(playerData); }
}
package fr.skyblock.models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class Island {
    
    private final String id;
    private final UUID ownerId;
    private final Location center;
    private final World world;
    
    private String name;
    private int size;
    private String biome;
    private boolean pvpEnabled;
    private int level;
    private long creationTime;
    private long lastActivity;
    
    private final Set<UUID> members;
    private final Set<UUID> banned;
    private final Map<UUID, IslandRole> roles;
    private final Map<String, Boolean> settings;
    
    public Island(String id, UUID ownerId, Location center, World world) {
        this.id = id;
        this.ownerId = ownerId;
        this.center = center;
        this.world = world;
        this.name = "Île de " + getOwnerName();
        this.size = 100;
        this.biome = "PLAINS";
        this.pvpEnabled = false;
        this.level = 0;
        this.creationTime = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        
        this.members = new HashSet<>();
        this.banned = new HashSet<>();
        this.roles = new HashMap<>();
        this.settings = new HashMap<>();
        
        this.members.add(ownerId);
        this.roles.put(ownerId, IslandRole.OWNER);
        
        initializeDefaultSettings();
    }
    
    private void initializeDefaultSettings() {
        settings.put("allow_visitors", true);
        settings.put("allow_build", false);
        settings.put("allow_break", false);
        settings.put("allow_interact", false);
        settings.put("allow_chest_access", false);
        settings.put("allow_mob_damage", true);
        settings.put("allow_animal_damage", false);
    }
    
    private String getOwnerName() {
        Player owner = org.bukkit.Bukkit.getPlayer(ownerId);
        return owner != null ? owner.getName() : "Inconnu";
    }
    
    public boolean isInBounds(Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        double distance = center.distance(location);
        return distance <= size / 2.0;
    }
    
    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }
    
    public boolean isBanned(UUID playerId) {
        return banned.contains(playerId);
    }
    
    public boolean canBuild(UUID playerId) {
        IslandRole role = roles.get(playerId);
        return role != null && (role == IslandRole.OWNER || role == IslandRole.CO_OWNER || 
                               (role == IslandRole.MEMBER && settings.get("allow_build")));
    }
    
    public boolean canBreak(UUID playerId) {
        IslandRole role = roles.get(playerId);
        return role != null && (role == IslandRole.OWNER || role == IslandRole.CO_OWNER || 
                               (role == IslandRole.MEMBER && settings.get("allow_break")));
    }
    
    public boolean canInteract(UUID playerId) {
        IslandRole role = roles.get(playerId);
        return role != null && (role == IslandRole.OWNER || role == IslandRole.CO_OWNER || 
                               (role == IslandRole.MEMBER && settings.get("allow_interact")));
    }
    
    public void addMember(UUID playerId, IslandRole role) {
        members.add(playerId);
        roles.put(playerId, role);
        banned.remove(playerId);
    }
    
    public void removeMember(UUID playerId) {
        members.remove(playerId);
        roles.remove(playerId);
    }
    
    public void banMember(UUID playerId) {
        banned.add(playerId);
        members.remove(playerId);
        roles.remove(playerId);
    }
    
    public void unbanMember(UUID playerId) {
        banned.remove(playerId);
    }
    
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public Location getCenter() { return center; }
    public World getWorld() { return world; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getBiome() { return biome; }
    public void setBiome(String biome) { this.biome = biome; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getCreationTime() { return creationTime; }
    public long getLastActivity() { return lastActivity; }
    public Set<UUID> getMembers() { return new HashSet<>(members); }
    public Set<UUID> getBanned() { return new HashSet<>(banned); }
    public Map<UUID, IslandRole> getRoles() { return new HashMap<>(roles); }
    public Map<String, Boolean> getSettings() { return new HashMap<>(settings); }
    
    public void setSetting(String setting, boolean value) {
        settings.put(setting, value);
    }
    
    public boolean getSetting(String setting) {
        return settings.getOrDefault(setting, false);
    }
}
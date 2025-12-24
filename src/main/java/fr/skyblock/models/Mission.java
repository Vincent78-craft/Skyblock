package fr.skyblock.models;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Mission {
    
    private final String id;
    private final String name;
    private final String description;
    private final MissionType type;
    private final Material icon;
    private final int targetAmount;
    private final String targetData;
    
    private final int skyCoinsReward;
    private final List<ItemStack> itemRewards;
    private final boolean repeatable;
    private final int maxCompletions;
    
    public Mission(String id, String name, String description, MissionType type, 
                   Material icon, int targetAmount, String targetData,
                   int skyCoinsReward, List<ItemStack> itemRewards, 
                   boolean repeatable, int maxCompletions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.icon = icon;
        this.targetAmount = targetAmount;
        this.targetData = targetData;
        this.skyCoinsReward = skyCoinsReward;
        this.itemRewards = itemRewards;
        this.repeatable = repeatable;
        this.maxCompletions = maxCompletions;
    }
    
    public boolean canComplete(SkyPlayer player) {
        if (!repeatable && player.hasMission(id)) {
            return false;
        }
        
        if (maxCompletions > 0 && player.getMissionCompletions(id) >= maxCompletions) {
            return false;
        }
        
        return true;
    }
    
    public boolean checkProgress(SkyPlayer player, Object progressData) {
        switch (type) {
            case BREAK_BLOCKS:
                if (progressData instanceof Material) {
                    Material brokenMaterial = (Material) progressData;
                    if (targetData == null || targetData.equals(brokenMaterial.name())) {
                        int current = (int) player.getPlayerData("mission_" + id + "_progress", 0);
                        current++;
                        player.setPlayerData("mission_" + id + "_progress", current);
                        return current >= targetAmount;
                    }
                }
                break;
                
            case KILL_MOBS:
                if (progressData instanceof EntityType) {
                    EntityType killedEntity = (EntityType) progressData;
                    if (targetData == null || targetData.equals(killedEntity.name())) {
                        int current = (int) player.getPlayerData("mission_" + id + "_progress", 0);
                        current++;
                        player.setPlayerData("mission_" + id + "_progress", current);
                        return current >= targetAmount;
                    }
                }
                break;
                
            case COLLECT_ITEMS:
                if (progressData instanceof Material) {
                    Material collectedMaterial = (Material) progressData;
                    if (targetData == null || targetData.equals(collectedMaterial.name())) {
                        int current = (int) player.getPlayerData("mission_" + id + "_progress", 0);
                        current++;
                        player.setPlayerData("mission_" + id + "_progress", current);
                        return current >= targetAmount;
                    }
                }
                break;
                
            case SPEND_COINS:
                if (progressData instanceof Integer) {
                    int spent = (int) progressData;
                    int current = (int) player.getPlayerData("mission_" + id + "_progress", 0);
                    current += spent;
                    player.setPlayerData("mission_" + id + "_progress", current);
                    return current >= targetAmount;
                }
                break;
        }
        
        return false;
    }
    
    public int getCurrentProgress(SkyPlayer player) {
        return (int) player.getPlayerData("mission_" + id + "_progress", 0);
    }
    
    public void resetProgress(SkyPlayer player) {
        player.setPlayerData("mission_" + id + "_progress", 0);
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public MissionType getType() { return type; }
    public Material getIcon() { return icon; }
    public int getTargetAmount() { return targetAmount; }
    public String getTargetData() { return targetData; }
    public int getSkyCoinsReward() { return skyCoinsReward; }
    public List<ItemStack> getItemRewards() { return itemRewards; }
    public boolean isRepeatable() { return repeatable; }
    public int getMaxCompletions() { return maxCompletions; }
}
package fr.skyblock.managers;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.Mission;
import fr.skyblock.models.MissionType;
import fr.skyblock.models.SkyPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissionManager {
    
    private final SkyBlockPlugin plugin;
    private final Map<String, Mission> missions;
    
    public MissionManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        this.missions = new HashMap<>();
        
        initializeDefaultMissions();
    }
    
    private void initializeDefaultMissions() {
        // Mission 1: Casser des blocs de pierre
        missions.put("stone_breaker", new Mission(
            "stone_breaker",
            "Mineur Débutant",
            "Cassez 100 blocs de pierre pour prouver votre détermination",
            MissionType.BREAK_BLOCKS,
            Material.STONE,
            100,
            "STONE",
            250,
            new ArrayList<>(),
            true,
            -1
        ));
        
        // Mission 2: Collecter du bois
        missions.put("wood_collector", new Mission(
            "wood_collector",
            "Bûcheron",
            "Collectez 64 bûches de chêne pour vos constructions",
            MissionType.COLLECT_ITEMS,
            Material.OAK_LOG,
            64,
            "OAK_LOG",
            150,
            new ArrayList<>(),
            true,
            -1
        ));
        
        // Mission 3: Crafter des objets
        missions.put("crafter", new Mission(
            "crafter",
            "Artisan",
            "Craftez 32 planches de bois",
            MissionType.CRAFT_ITEMS,
            Material.OAK_PLANKS,
            32,
            "OAK_PLANKS",
            100,
            new ArrayList<>(),
            true,
            -1
        ));
        
        // Mission 4: Économiser
        missions.put("saver", new Mission(
            "saver",
            "Économe",
            "Économisez 1000 SkyCoins",
            MissionType.SPEND_COINS,
            Material.GOLD_INGOT,
            1000,
            null,
            500,
            new ArrayList<>(),
            false,
            1
        ));
        
        // Mission 5: Explorer
        missions.put("explorer", new Mission(
            "explorer",
            "Explorateur",
            "Visitez 5 îles différentes",
            MissionType.VISIT_ISLANDS,
            Material.COMPASS,
            5,
            null,
            400,
            new ArrayList<>(),
            false,
            1
        ));
        
        plugin.getLogger().info("Missions par défaut initialisées : " + missions.size() + " missions");
    }
    
    public Mission getMission(String missionId) {
        return missions.get(missionId);
    }
    
    public List<Mission> getAllMissions() {
        return new ArrayList<>(missions.values());
    }
    
    public List<Mission> getAvailableMissions(SkyPlayer player) {
        List<Mission> available = new ArrayList<>();
        
        for (Mission mission : missions.values()) {
            if (mission.canComplete(player)) {
                available.add(mission);
            }
        }
        
        return available;
    }
    
    public boolean completeMission(SkyPlayer player, String missionId) {
        Mission mission = missions.get(missionId);
        
        if (mission == null || !mission.canComplete(player)) {
            return false;
        }
        
        // Donner les récompenses
        plugin.getEconomyManager().addSkyCoins(player.getUuid(), mission.getSkyCoinsReward());
        
        // Donner les objets de récompense
        for (ItemStack item : mission.getItemRewards()) {
            // TODO: Donner les objets au joueur
        }
        
        // Marquer la mission comme terminée
        player.completeMission(missionId);
        mission.resetProgress(player);
        
        plugin.getLogger().info("Mission terminée : " + missionId + " par " + player.getName());
        return true;
    }
    
    public void updateMissionProgress(SkyPlayer player, MissionType type, Object data) {
        for (Mission mission : missions.values()) {
            if (mission.getType() == type && mission.canComplete(player)) {
                if (mission.checkProgress(player, data)) {
                    // Mission terminée automatiquement
                    completeMission(player, mission.getId());
                }
            }
        }
    }
    
    public void onBlockBreak(SkyPlayer player, Material material) {
        updateMissionProgress(player, MissionType.BREAK_BLOCKS, material);
    }
    
    public void onItemCollect(SkyPlayer player, Material material) {
        updateMissionProgress(player, MissionType.COLLECT_ITEMS, material);
    }
    
    public void onItemCraft(SkyPlayer player, Material material) {
        updateMissionProgress(player, MissionType.CRAFT_ITEMS, material);
    }
    
    public void onCoinsSpent(SkyPlayer player, int amount) {
        updateMissionProgress(player, MissionType.SPEND_COINS, amount);
    }
    
    public void onIslandVisit(SkyPlayer player) {
        updateMissionProgress(player, MissionType.VISIT_ISLANDS, null);
    }
    
    public void addMission(Mission mission) {
        missions.put(mission.getId(), mission);
        plugin.getLogger().info("Mission ajoutée : " + mission.getName());
    }
    
    public void removeMission(String missionId) {
        missions.remove(missionId);
        plugin.getLogger().info("Mission supprimée : " + missionId);
    }
    
    public void reloadMissions() {
        missions.clear();
        initializeDefaultMissions();
        // TODO: Charger les missions depuis la configuration
        plugin.getLogger().info("Missions rechargées !");
    }
    
    public int getTotalMissions() {
        return missions.size();
    }
    
    public int getCompletedMissions(SkyPlayer player) {
        int completed = 0;
        for (Mission mission : missions.values()) {
            if (player.hasMission(mission.getId())) {
                completed++;
            }
        }
        return completed;
    }
    
    public double getCompletionPercentage(SkyPlayer player) {
        if (missions.isEmpty()) {
            return 0.0;
        }
        
        int completed = getCompletedMissions(player);
        return (double) completed / missions.size() * 100.0;
    }
    
    public List<Mission> getDailyMissions() {
        // TODO: Implémenter les missions quotidiennes
        return new ArrayList<>();
    }
    
    public List<Mission> getWeeklyMissions() {
        // TODO: Implémenter les missions hebdomadaires
        return new ArrayList<>();
    }
    
    public void resetDailyMissions() {
        // TODO: Reset des missions quotidiennes
        plugin.getLogger().info("Missions quotidiennes réinitialisées !");
    }
    
    public void resetWeeklyMissions() {
        // TODO: Reset des missions hebdomadaires
        plugin.getLogger().info("Missions hebdomadaires réinitialisées !");
    }
}
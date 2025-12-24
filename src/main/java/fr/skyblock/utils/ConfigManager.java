package fr.skyblock.utils;

import fr.skyblock.SkyBlockPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    
    private final SkyBlockPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private FileConfiguration shopConfig;
    private FileConfiguration missionsConfig;
    
    public ConfigManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    private void loadConfigs() {
        saveDefaultConfigs();
        
        config = plugin.getConfig();
        messagesConfig = loadConfig("messages.yml");
        shopConfig = loadConfig("shop.yml");
        missionsConfig = loadConfig("missions.yml");
    }
    
    private void saveDefaultConfigs() {
        plugin.saveDefaultConfig();
        saveDefaultConfig("messages.yml");
        saveDefaultConfig("shop.yml");
        saveDefaultConfig("missions.yml");
    }
    
    private void saveDefaultConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
    
    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(file);
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        loadConfigs();
        plugin.getLogger().info("Configuration rechargée !");
    }
    
    public void saveConfig(String configName) {
        try {
            switch (configName.toLowerCase()) {
                case "messages":
                    messagesConfig.save(new File(plugin.getDataFolder(), "messages.yml"));
                    break;
                case "shop":
                    shopConfig.save(new File(plugin.getDataFolder(), "shop.yml"));
                    break;
                case "missions":
                    missionsConfig.save(new File(plugin.getDataFolder(), "missions.yml"));
                    break;
                default:
                    plugin.saveConfig();
                    break;
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde de " + configName + ".yml");
            e.printStackTrace();
        }
    }
    
    // Configuration générale
    public int getDefaultIslandSize() {
        return config.getInt("island.default-size", 100);
    }
    
    public int getMaxIslandSize() {
        return config.getInt("island.max-size", 500);
    }
    
    public int getMaxIslandMembers() {
        return config.getInt("island.max-members", 10);
    }
    
    public int getStarterCoins() {
        return config.getInt("economy.starter-coins", 100);
    }
    
    public boolean isPvpEnabledByDefault() {
        return config.getBoolean("island.pvp-enabled-by-default", false);
    }
    
    public List<String> getAvailableBiomes() {
        return config.getStringList("island.available-biomes");
    }
    
    public int getIslandSpacing() {
        return config.getInt("island.spacing", 1000);
    }
    
    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", true);
    }
    
    // Messages
    public String getMessage(String key) {
        return messagesConfig.getString(key, "§cMessage manquant: " + key);
    }
    
    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        return message;
    }
    
    // Shop
    public FileConfiguration getShopConfig() {
        return shopConfig;
    }
    
    public int getShopItemPrice(String category, String item) {
        return shopConfig.getInt("categories." + category + ".items." + item + ".price", 0);
    }
    
    public boolean isShopItemEnabled(String category, String item) {
        return shopConfig.getBoolean("categories." + category + ".items." + item + ".enabled", true);
    }
    
    // Missions
    public FileConfiguration getMissionsConfig() {
        return missionsConfig;
    }
    
    public List<String> getAvailableMissions() {
        return List.copyOf(missionsConfig.getConfigurationSection("missions").getKeys(false));
    }
    
    public int getMissionReward(String missionId) {
        return missionsConfig.getInt("missions." + missionId + ".reward.skycoins", 0);
    }
    
    public boolean isMissionEnabled(String missionId) {
        return missionsConfig.getBoolean("missions." + missionId + ".enabled", true);
    }
    
    // Méthodes utilitaires pour la configuration
    public void setValue(String path, Object value) {
        config.set(path, value);
        plugin.saveConfig();
    }
    
    public void setMessageValue(String path, Object value) {
        messagesConfig.set(path, value);
        saveConfig("messages");
    }
    
    public void setShopValue(String path, Object value) {
        shopConfig.set(path, value);
        saveConfig("shop");
    }
    
    public void setMissionValue(String path, Object value) {
        missionsConfig.set(path, value);
        saveConfig("missions");
    }
    
    // Valeurs par défaut pour le debug
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }
    
    public boolean isAutoSaveEnabled() {
        return config.getBoolean("auto-save.enabled", true);
    }
    
    public int getAutoSaveInterval() {
        return config.getInt("auto-save.interval", 300); // 5 minutes par défaut
    }
    
    public int getTopIslandsLimit() {
        return config.getInt("leaderboard.max-islands", 10);
    }
    
    public boolean areVisitorsAllowedByDefault() {
        return config.getBoolean("island.allow-visitors-by-default", true);
    }
    
    public int getIslandLevelCalculationInterval() {
        return config.getInt("island.level-calculation-interval", 60); // 1 minute par défaut
    }
}
package fr.skyblock;

import fr.skyblock.commands.IslandCommand;
import fr.skyblock.commands.SkyCoinsCommand;
import fr.skyblock.database.DatabaseManager;
import fr.skyblock.listeners.IslandListener;
import fr.skyblock.listeners.ProtectionListener;
import fr.skyblock.managers.*;
import fr.skyblock.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkyBlockPlugin extends JavaPlugin {

    private static SkyBlockPlugin instance;
    
    private DatabaseManager databaseManager;
    private IslandManager islandManager;
    private EconomyManager economyManager;
    private MissionManager missionManager;
    private ShopManager shopManager;
    private ConfigManager configManager;
    private WorldManager worldManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("Démarrage du plugin SkyBlock v" + getDescription().getVersion());
        
        saveDefaultConfig();
        initializeManagers();
        registerCommands();
        registerListeners();
        
        getLogger().info("Plugin SkyBlock chargé avec succès !");
    }

    @Override
    public void onDisable() {
        getLogger().info("Arrêt du plugin SkyBlock...");
        
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        getLogger().info("Plugin SkyBlock arrêté !");
    }
    
    private void initializeManagers() {
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        worldManager = new WorldManager(this);
        economyManager = new EconomyManager(this);
        islandManager = new IslandManager(this);
        missionManager = new MissionManager(this);
        shopManager = new ShopManager(this);
        scoreboardManager = new ScoreboardManager(this);
    }
    
    private void registerCommands() {
        getCommand("island").setExecutor(new IslandCommand(this));
        getCommand("skycoins").setExecutor(new SkyCoinsCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new IslandListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
    }
    
    public static SkyBlockPlugin getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public IslandManager getIslandManager() {
        return islandManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public MissionManager getMissionManager() {
        return missionManager;
    }
    
    public ShopManager getShopManager() {
        return shopManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public WorldManager getWorldManager() {
        return worldManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
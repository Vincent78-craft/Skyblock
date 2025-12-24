package fr.skyblock.database;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.*;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    
    private final SkyBlockPlugin plugin;
    private Connection connection;
    
    public DatabaseManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        setupDatabase();
    }
    
    private void setupDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/skyblock.db";
            connection = DriverManager.getConnection(url);
            
            createTables();
            plugin.getLogger().info("Base de données SQLite initialisée !");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'initialisation de la base de données !");
            e.printStackTrace();
        }
    }
    
    private void createTables() throws SQLException {
        String createIslandsTable = """
            CREATE TABLE IF NOT EXISTS islands (
                id TEXT PRIMARY KEY,
                owner_uuid TEXT NOT NULL,
                center_x DOUBLE NOT NULL,
                center_y DOUBLE NOT NULL,
                center_z DOUBLE NOT NULL,
                world_name TEXT NOT NULL,
                name TEXT NOT NULL,
                size INTEGER DEFAULT 100,
                biome TEXT DEFAULT 'PLAINS',
                pvp_enabled BOOLEAN DEFAULT FALSE,
                level INTEGER DEFAULT 0,
                creation_time BIGINT NOT NULL,
                last_activity BIGINT NOT NULL
            );
            """;
            
        String createPlayersTable = """
            CREATE TABLE IF NOT EXISTS players (
                uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                current_island_id TEXT,
                sky_coins INTEGER DEFAULT 0,
                last_login BIGINT NOT NULL
            );
            """;
            
        String createIslandMembersTable = """
            CREATE TABLE IF NOT EXISTS island_members (
                island_id TEXT NOT NULL,
                player_uuid TEXT NOT NULL,
                role TEXT NOT NULL,
                PRIMARY KEY (island_id, player_uuid),
                FOREIGN KEY (island_id) REFERENCES islands(id),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid)
            );
            """;
            
        String createIslandBannedTable = """
            CREATE TABLE IF NOT EXISTS island_banned (
                island_id TEXT NOT NULL,
                player_uuid TEXT NOT NULL,
                banned_time BIGINT NOT NULL,
                PRIMARY KEY (island_id, player_uuid),
                FOREIGN KEY (island_id) REFERENCES islands(id),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid)
            );
            """;
            
        String createIslandSettingsTable = """
            CREATE TABLE IF NOT EXISTS island_settings (
                island_id TEXT NOT NULL,
                setting_key TEXT NOT NULL,
                setting_value TEXT NOT NULL,
                PRIMARY KEY (island_id, setting_key),
                FOREIGN KEY (island_id) REFERENCES islands(id)
            );
            """;
            
        String createPlayerMissionsTable = """
            CREATE TABLE IF NOT EXISTS player_missions (
                player_uuid TEXT NOT NULL,
                mission_id TEXT NOT NULL,
                completions INTEGER DEFAULT 0,
                last_completion BIGINT,
                PRIMARY KEY (player_uuid, mission_id),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid)
            );
            """;
            
        String createPlayerDataTable = """
            CREATE TABLE IF NOT EXISTS player_data (
                player_uuid TEXT NOT NULL,
                data_key TEXT NOT NULL,
                data_value TEXT NOT NULL,
                PRIMARY KEY (player_uuid, data_key),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid)
            );
            """;
        
        Statement stmt = connection.createStatement();
        stmt.execute(createIslandsTable);
        stmt.execute(createPlayersTable);
        stmt.execute(createIslandMembersTable);
        stmt.execute(createIslandBannedTable);
        stmt.execute(createIslandSettingsTable);
        stmt.execute(createPlayerMissionsTable);
        stmt.execute(createPlayerDataTable);
        stmt.close();
    }
    
    public CompletableFuture<Void> saveIsland(Island island) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = """
                    INSERT OR REPLACE INTO islands 
                    (id, owner_uuid, center_x, center_y, center_z, world_name, name, size, biome, pvp_enabled, level, creation_time, last_activity)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, island.getId());
                stmt.setString(2, island.getOwnerId().toString());
                stmt.setDouble(3, island.getCenter().getX());
                stmt.setDouble(4, island.getCenter().getY());
                stmt.setDouble(5, island.getCenter().getZ());
                stmt.setString(6, island.getWorld().getName());
                stmt.setString(7, island.getName());
                stmt.setInt(8, island.getSize());
                stmt.setString(9, island.getBiome());
                stmt.setBoolean(10, island.isPvpEnabled());
                stmt.setInt(11, island.getLevel());
                stmt.setLong(12, island.getCreationTime());
                stmt.setLong(13, island.getLastActivity());
                stmt.executeUpdate();
                stmt.close();
                
                saveIslandMembers(island);
                saveIslandSettings(island);
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Erreur lors de la sauvegarde de l'île " + island.getId());
                e.printStackTrace();
            }
        });
    }
    
    public CompletableFuture<SkyPlayer> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT * FROM players WHERE uuid = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                SkyPlayer player = null;
                if (rs.next()) {
                    player = new SkyPlayer(uuid, rs.getString("name"));
                    player.setCurrentIslandId(rs.getString("current_island_id"));
                    player.setSkyCoins(rs.getInt("sky_coins"));
                    
                    loadPlayerMissions(player);
                    loadPlayerData(player);
                }
                
                rs.close();
                stmt.close();
                return player;
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Erreur lors du chargement du joueur " + uuid);
                e.printStackTrace();
                return null;
            }
        });
    }
    
    private void saveIslandMembers(Island island) throws SQLException {
        String deleteSql = "DELETE FROM island_members WHERE island_id = ?";
        PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
        deleteStmt.setString(1, island.getId());
        deleteStmt.executeUpdate();
        deleteStmt.close();
        
        String insertSql = "INSERT OR REPLACE INTO island_members (island_id, player_uuid, role) VALUES (?, ?, ?)";
        PreparedStatement insertStmt = connection.prepareStatement(insertSql);
        
        for (Map.Entry<UUID, IslandRole> entry : island.getRoles().entrySet()) {
            insertStmt.setString(1, island.getId());
            insertStmt.setString(2, entry.getKey().toString());
            insertStmt.setString(3, entry.getValue().name());
            insertStmt.executeUpdate();
        }
        insertStmt.close();
    }
    
    private void saveIslandSettings(Island island) throws SQLException {
        String deleteSql = "DELETE FROM island_settings WHERE island_id = ?";
        PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
        deleteStmt.setString(1, island.getId());
        deleteStmt.executeUpdate();
        deleteStmt.close();
        
        String insertSql = "INSERT OR REPLACE INTO island_settings (island_id, setting_key, setting_value) VALUES (?, ?, ?)";
        PreparedStatement insertStmt = connection.prepareStatement(insertSql);
        
        for (Map.Entry<String, Boolean> entry : island.getSettings().entrySet()) {
            insertStmt.setString(1, island.getId());
            insertStmt.setString(2, entry.getKey());
            insertStmt.setString(3, entry.getValue().toString());
            insertStmt.executeUpdate();
        }
        insertStmt.close();
    }
    
    private void loadPlayerMissions(SkyPlayer player) throws SQLException {
        String sql = "SELECT * FROM player_missions WHERE player_uuid = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, player.getUuid().toString());
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            String missionId = rs.getString("mission_id");
            int completions = rs.getInt("completions");
            
            for (int i = 0; i < completions; i++) {
                player.completeMission(missionId);
            }
        }
        
        rs.close();
        stmt.close();
    }
    
    private void loadPlayerData(SkyPlayer player) throws SQLException {
        String sql = "SELECT * FROM player_data WHERE player_uuid = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, player.getUuid().toString());
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            String key = rs.getString("data_key");
            String value = rs.getString("data_value");
            
            try {
                if (value.equals("true") || value.equals("false")) {
                    player.setPlayerData(key, Boolean.parseBoolean(value));
                } else if (value.matches("-?\\d+")) {
                    player.setPlayerData(key, Integer.parseInt(value));
                } else {
                    player.setPlayerData(key, value);
                }
            } catch (Exception e) {
                player.setPlayerData(key, value);
            }
        }
        
        rs.close();
        stmt.close();
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la fermeture de la base de données !");
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
}
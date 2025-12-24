package fr.skyblock.managers;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.SkyPlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {
    
    private final SkyBlockPlugin plugin;
    private final Map<UUID, Integer> skyCoinsCache;
    
    public EconomyManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        this.skyCoinsCache = new ConcurrentHashMap<>();
    }
    
    public int getSkyCoins(UUID playerId) {
        return skyCoinsCache.getOrDefault(playerId, 0);
    }
    
    public void setSkyCoins(UUID playerId, int amount) {
        if (amount < 0) {
            amount = 0;
        }
        
        skyCoinsCache.put(playerId, amount);
        savePlayerCoins(playerId, amount);
    }
    
    public void addSkyCoins(UUID playerId, int amount) {
        if (amount <= 0) {
            return;
        }
        
        int currentCoins = getSkyCoins(playerId);
        int newAmount = currentCoins + amount;
        
        skyCoinsCache.put(playerId, newAmount);
        savePlayerCoins(playerId, newAmount);
        
        // Mettre à jour le scoreboard
        plugin.getScoreboardManager().updatePlayerEconomy(playerId);
    }
    
    public boolean removeSkyCoins(UUID playerId, int amount) {
        if (amount <= 0) {
            return false;
        }
        
        int currentCoins = getSkyCoins(playerId);
        if (currentCoins < amount) {
            return false;
        }
        
        int newAmount = currentCoins - amount;
        skyCoinsCache.put(playerId, newAmount);
        savePlayerCoins(playerId, newAmount);
        
        // Mettre à jour le scoreboard
        plugin.getScoreboardManager().updatePlayerEconomy(playerId);
        
        return true;
    }
    
    public boolean hasEnoughSkyCoins(UUID playerId, int amount) {
        return getSkyCoins(playerId) >= amount;
    }
    
    public boolean transferSkyCoins(UUID fromPlayer, UUID toPlayer, int amount) {
        if (amount <= 0) {
            return false;
        }
        
        if (!hasEnoughSkyCoins(fromPlayer, amount)) {
            return false;
        }
        
        removeSkyCoins(fromPlayer, amount);
        addSkyCoins(toPlayer, amount);
        
        return true;
    }
    
    public CompletableFuture<Void> loadPlayerEconomy(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            SkyPlayer skyPlayer = plugin.getDatabaseManager().loadPlayer(playerId).join();
            
            if (skyPlayer != null) {
                skyCoinsCache.put(playerId, skyPlayer.getSkyCoins());
            } else {
                skyCoinsCache.put(playerId, 0);
            }
        });
    }
    
    public void unloadPlayerEconomy(UUID playerId) {
        skyCoinsCache.remove(playerId);
    }
    
    private void savePlayerCoins(UUID playerId, int amount) {
        CompletableFuture.runAsync(() -> {
            try {
                String sql = "UPDATE players SET sky_coins = ? WHERE uuid = ?";
                PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(sql);
                stmt.setInt(1, amount);
                stmt.setString(2, playerId.toString());
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    // Utiliser INSERT OR REPLACE pour éviter les conflits
                    String insertSql = "INSERT OR REPLACE INTO players (uuid, name, sky_coins, last_login) VALUES (?, ?, ?, ?)";
                    PreparedStatement insertStmt = plugin.getDatabaseManager().getConnection().prepareStatement(insertSql);
                    insertStmt.setString(1, playerId.toString());
                    
                    var player = plugin.getServer().getPlayer(playerId);
                    String playerName = player != null ? player.getName() : "Unknown";
                    insertStmt.setString(2, playerName);
                    insertStmt.setInt(3, amount);
                    insertStmt.setLong(4, System.currentTimeMillis());
                    insertStmt.executeUpdate();
                    insertStmt.close();
                }
                
                stmt.close();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Erreur lors de la sauvegarde des SkyCoins pour " + playerId);
                e.printStackTrace();
            }
        });
    }
    
    public String formatSkyCoins(int amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        } else {
            return String.valueOf(amount);
        }
    }
    
    public int getTotalSkyCoins() {
        return skyCoinsCache.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public Map<UUID, Integer> getTopPlayers(int limit) {
        return skyCoinsCache.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    java.util.LinkedHashMap::new
                ));
    }
    
    public void saveAllPlayerCoins() {
        for (Map.Entry<UUID, Integer> entry : skyCoinsCache.entrySet()) {
            savePlayerCoins(entry.getKey(), entry.getValue());
        }
    }
    
    public void resetPlayerEconomy(UUID playerId) {
        setSkyCoins(playerId, 0);
    }
    
    public void giveStarterCoins(UUID playerId) {
        int starterAmount = plugin.getConfigManager().getStarterCoins();
        addSkyCoins(playerId, starterAmount);
    }
    
    public boolean isValidAmount(int amount) {
        return amount >= 0 && amount <= 999999999;
    }
    
    public int calculateTax(int amount, double taxRate) {
        return (int) Math.ceil(amount * taxRate);
    }
    
    public boolean purchaseItem(UUID playerId, int cost) {
        return purchaseItem(playerId, cost, 0.0);
    }
    
    public boolean purchaseItem(UUID playerId, int cost, double taxRate) {
        int totalCost = cost + calculateTax(cost, taxRate);
        return removeSkyCoins(playerId, totalCost);
    }
}
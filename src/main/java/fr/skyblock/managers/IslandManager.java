package fr.skyblock.managers;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.Island;
import fr.skyblock.models.IslandRole;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandManager {
    
    private final SkyBlockPlugin plugin;
    private final Map<String, Island> islands;
    private final Map<UUID, String> playerIslands;
    private int nextIslandId;
    
    public IslandManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        this.islands = new ConcurrentHashMap<>();
        this.playerIslands = new ConcurrentHashMap<>();
        this.nextIslandId = 1;
        
        loadAllIslands();
    }
    
    private void loadAllIslands() {
        // TODO: Charger toutes les îles depuis la base de données
        plugin.getLogger().info("Chargement des îles depuis la base de données...");
    }
    
    public Island createIsland(Player player, String schematicName) {
        if (hasIsland(player.getUniqueId())) {
            return getPlayerIsland(player.getUniqueId());
        }
        
        World skyWorld = plugin.getWorldManager().getSkyWorld();
        Location center = findNextIslandLocation(skyWorld);
        
        String islandId = "island_" + nextIslandId++;
        Island island = new Island(islandId, player.getUniqueId(), center, skyWorld);
        
        islands.put(islandId, island);
        playerIslands.put(player.getUniqueId(), islandId);
        
        // Générer l'île avec le schéma choisi
        plugin.getWorldManager().generateIsland(center, schematicName);
        
        // Sauvegarder en base de données
        plugin.getDatabaseManager().saveIsland(island);
        
        // Mettre à jour le scoreboard
        plugin.getScoreboardManager().updatePlayerIsland(player.getUniqueId());
        
        plugin.getLogger().info("Île créée pour " + player.getName() + " (ID: " + islandId + ")");
        return island;
    }
    
    public boolean deleteIsland(String islandId) {
        Island island = islands.get(islandId);
        if (island == null) {
            return false;
        }
        
        // Supprimer tous les membres de l'île
        for (UUID memberId : island.getMembers()) {
            playerIslands.remove(memberId);
        }
        
        islands.remove(islandId);
        
        // TODO: Supprimer de la base de données
        // TODO: Nettoyer les blocs dans le monde
        
        plugin.getLogger().info("Île supprimée : " + islandId);
        return true;
    }
    
    public Island getIsland(String islandId) {
        return islands.get(islandId);
    }
    
    public Island getPlayerIsland(UUID playerId) {
        String islandId = playerIslands.get(playerId);
        return islandId != null ? islands.get(islandId) : null;
    }
    
    public boolean hasIsland(UUID playerId) {
        return playerIslands.containsKey(playerId);
    }
    
    public Island getIslandAt(Location location) {
        for (Island island : islands.values()) {
            if (island.isInBounds(location)) {
                return island;
            }
        }
        return null;
    }
    
    public void invitePlayer(Island island, UUID invitedPlayer, IslandRole role) {
        island.addMember(invitedPlayer, role);
        playerIslands.put(invitedPlayer, island.getId());
        
        plugin.getDatabaseManager().saveIsland(island);
    }
    
    public void kickPlayer(Island island, UUID kickedPlayer) {
        island.removeMember(kickedPlayer);
        playerIslands.remove(kickedPlayer);
        
        plugin.getDatabaseManager().saveIsland(island);
    }
    
    public void banPlayer(Island island, UUID bannedPlayer) {
        island.banMember(bannedPlayer);
        playerIslands.remove(bannedPlayer);
        
        plugin.getDatabaseManager().saveIsland(island);
    }
    
    public List<Island> getTopIslands(int limit) {
        return islands.values().stream()
                .sorted((a, b) -> Integer.compare(b.getLevel(), a.getLevel()))
                .limit(limit)
                .toList();
    }
    
    public void updateIslandLevel(Island island) {
        // TODO: Calculer le niveau de l'île basé sur les blocs
        int newLevel = calculateIslandLevel(island);
        island.setLevel(newLevel);
        plugin.getDatabaseManager().saveIsland(island);
        
        // Mettre à jour le scoreboard du propriétaire et des membres
        plugin.getScoreboardManager().updatePlayerIsland(island.getOwnerId());
        for (UUID memberId : island.getMembers()) {
            plugin.getScoreboardManager().updatePlayerIsland(memberId);
        }
    }
    
    private int calculateIslandLevel(Island island) {
        // Calcul basique du niveau basé sur les blocs présents
        // TODO: Implémenter un système de calcul plus avancé
        return island.getLevel() + 1;
    }
    
    private Location findNextIslandLocation(World world) {
        int spacing = 1000; // Espacement entre les îles
        int x = 0, z = 0;
        
        // Trouver une position libre en spirale
        int step = 1;
        int direction = 0; // 0=droite, 1=bas, 2=gauche, 3=haut
        
        for (int i = 0; i < nextIslandId; i++) {
            if (i > 0) {
                switch (direction) {
                    case 0: x += spacing; break;
                    case 1: z += spacing; break;
                    case 2: x -= spacing; break;
                    case 3: z -= spacing; break;
                }
                
                if (i % (step * 2) == 0) {
                    direction = (direction + 1) % 4;
                    if (direction == 0 || direction == 2) {
                        step++;
                    }
                }
            }
        }
        
        return new Location(world, x, 100, z);
    }
    
    public void teleportToIsland(Player player, Island island) {
        Location center = island.getCenter();
        Location safeLocation = findSafeLocation(center);
        
        player.teleport(safeLocation);
        island.updateActivity();
        
        plugin.getDatabaseManager().saveIsland(island);
    }
    
    private Location findSafeLocation(Location center) {
        // Trouver un endroit sûr pour téléporter le joueur
        Location safe = center.clone();
        safe.setY(center.getY() + 1);
        
        // Vérifier que c'est un endroit sûr
        while (safe.getY() < 256 && 
               (safe.getBlock().getType().isSolid() || 
                safe.clone().add(0, 1, 0).getBlock().getType().isSolid())) {
            safe.setY(safe.getY() + 1);
        }
        
        return safe;
    }
    
    public Map<String, Island> getAllIslands() {
        return new HashMap<>(islands);
    }
    
    public int getTotalIslands() {
        return islands.size();
    }
    
    public void saveAllIslands() {
        for (Island island : islands.values()) {
            plugin.getDatabaseManager().saveIsland(island);
        }
    }
}
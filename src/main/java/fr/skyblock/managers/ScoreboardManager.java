package fr.skyblock.managers;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.Island;
import fr.skyblock.models.SkyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {
    
    private final SkyBlockPlugin plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, Long> lastUpdate;
    
    private final String BOARD_TITLE = "§6§l⭐ SkyBlock ⭐";
    private final long UPDATE_INTERVAL = 1000; // 1 seconde
    
    public ScoreboardManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new ConcurrentHashMap<>();
        this.lastUpdate = new ConcurrentHashMap<>();
        
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 20L, 20L); // Toutes les secondes
    }
    
    public void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("skyblock", "dummy", BOARD_TITLE);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
        
        updateScoreboard(player);
    }
    
    public void updateScoreboard(Player player) {
        if (!shouldUpdate(player)) {
            return;
        }
        
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            createScoreboard(player);
            return;
        }
        
        Objective objective = scoreboard.getObjective("skyblock");
        if (objective == null) {
            return;
        }
        
        // Effacer les anciennes entrées
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        // Construire le nouveau contenu
        List<String> lines = buildScoreboardLines(player);
        
        // Ajouter les lignes (de bas en haut car Minecraft inverse)
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(lines.size() - 1 - i);
            objective.getScore(line).setScore(i);
        }
        
        lastUpdate.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    private boolean shouldUpdate(Player player) {
        long lastUpdateTime = lastUpdate.getOrDefault(player.getUniqueId(), 0L);
        return System.currentTimeMillis() - lastUpdateTime >= UPDATE_INTERVAL;
    }
    
    private List<String> buildScoreboardLines(Player player) {
        return List.of(
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            "",
            "§6💰 SkyCoins:",
            "§f  " + formatNumber(plugin.getEconomyManager().getSkyCoins(player.getUniqueId())),
            "",
            buildIslandLine(player),
            buildIslandLevelLine(player),
            buildIslandMembersLine(player),
            "",
            buildRankLine(player),
            buildMissionsLine(player),
            "",
            "§7🌐 " + getFormattedDate(),
            "",
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        );
    }
    
    private String buildIslandLine(Player player) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            return "§c🏝️ Pas d'île";
        }
        
        String islandName = island.getName();
        if (islandName.length() > 15) {
            islandName = islandName.substring(0, 12) + "...";
        }
        
        return "§a🏝️ " + islandName;
    }
    
    private String buildIslandLevelLine(Player player) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            return "§7  Niveau: §f0";
        }
        
        return "§7  Niveau: §e" + island.getLevel();
    }
    
    private String buildIslandMembersLine(Player player) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            return "§7  Membres: §f0";
        }
        
        int membersCount = island.getMembers().size();
        return "§7  Membres: §b" + membersCount + "/10";
    }
    
    private String buildRankLine(Player player) {
        List<Island> topIslands = plugin.getIslandManager().getTopIslands(100);
        Island playerIsland = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        
        if (playerIsland == null) {
            return "§7📈 Rang: §fN/A";
        }
        
        for (int i = 0; i < topIslands.size(); i++) {
            if (topIslands.get(i).getId().equals(playerIsland.getId())) {
                return "§7📈 Rang: §6#" + (i + 1);
            }
        }
        
        return "§7📈 Rang: §f+" + topIslands.size();
    }
    
    private String buildMissionsLine(Player player) {
        try {
            SkyPlayer skyPlayer = plugin.getDatabaseManager().loadPlayer(player.getUniqueId()).join();
            if (skyPlayer == null) {
                return "§7🎯 Missions: §f0";
            }
            
            int completedMissions = plugin.getMissionManager().getCompletedMissions(skyPlayer);
            int totalMissions = plugin.getMissionManager().getTotalMissions();
            
            return "§7🎯 Missions: §a" + completedMissions + "§7/§f" + totalMissions;
        } catch (Exception e) {
            return "§7🎯 Missions: §f0";
        }
    }
    
    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }
    
    private String getFormattedDate() {
        return new java.text.SimpleDateFormat("dd/MM HH:mm").format(new java.util.Date());
    }
    
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        lastUpdate.remove(player.getUniqueId());
        
        // Remettre le scoreboard par défaut du serveur
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
    public void updatePlayerEconomy(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            forceUpdate(player);
        }
    }
    
    public void updatePlayerIsland(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            forceUpdate(player);
        }
    }
    
    public void updatePlayerMissions(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            forceUpdate(player);
        }
    }
    
    private void forceUpdate(Player player) {
        lastUpdate.remove(player.getUniqueId()); // Force la prochaine mise à jour
        updateScoreboard(player);
    }
    
    public void refreshAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            forceUpdate(player);
        }
    }
    
    public boolean hasScoreboard(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }
    
    public void toggleScoreboard(Player player) {
        if (hasScoreboard(player)) {
            removeScoreboard(player);
            player.sendMessage("§c📊 Scoreboard masqué !");
        } else {
            createScoreboard(player);
            player.sendMessage("§a📊 Scoreboard affiché !");
        }
    }
    
    public int getTotalPlayersWithScoreboard() {
        return playerScoreboards.size();
    }
}
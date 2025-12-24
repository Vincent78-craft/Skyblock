package fr.skyblock.listeners;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.SkyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class IslandListener implements Listener {
    
    private final SkyBlockPlugin plugin;
    
    public IslandListener(SkyBlockPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        plugin.getEconomyManager().loadPlayerEconomy(player.getUniqueId());
        
        // Créer le scoreboard après un léger délai pour laisser le joueur se connecter complètement
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getScoreboardManager().createScoreboard(player);
        }, 20L); // 1 seconde de délai
        
        if (player.hasPlayedBefore()) {
            if (!plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                player.sendMessage("§e🏝️ Bienvenue sur SkyBlock !");
                player.sendMessage("§7Utilisez §f/island §7pour créer votre île !");
            } else {
                var island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
                island.updateActivity();
                
                player.sendMessage("§a🏠 Bon retour sur votre île !");
                player.sendMessage("§7Utilisez §f/island §7pour accéder aux menus.");
            }
        } else {
            player.sendMessage("§6✨ Bienvenue sur le serveur SkyBlock !");
            player.sendMessage("§e🎯 Créez votre île avec §f/island §e!");
            player.sendMessage("§7💡 Tapez §f/island help §7pour obtenir de l'aide.");
            
            plugin.getEconomyManager().giveStarterCoins(player.getUniqueId());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        plugin.getEconomyManager().unloadPlayerEconomy(player.getUniqueId());
        plugin.getScoreboardManager().removeScoreboard(player);
        
        if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
            var island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
            island.updateActivity();
            plugin.getDatabaseManager().saveIsland(island);
        }
    }
}
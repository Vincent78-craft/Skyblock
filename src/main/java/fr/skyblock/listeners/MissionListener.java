package fr.skyblock.listeners;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.SkyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class MissionListener implements Listener {
    
    private final SkyBlockPlugin plugin;
    
    public MissionListener(SkyBlockPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        if (!plugin.getWorldManager().isInSkyWorld(player.getLocation())) {
            return;
        }
        
        SkyPlayer skyPlayer = plugin.getDatabaseManager().loadPlayer(player.getUniqueId()).join();
        if (skyPlayer != null) {
            plugin.getMissionManager().onBlockBreak(skyPlayer, event.getBlock().getType());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        if (!plugin.getWorldManager().isInSkyWorld(player.getLocation())) {
            return;
        }
        
        SkyPlayer skyPlayer = plugin.getDatabaseManager().loadPlayer(player.getUniqueId()).join();
        if (skyPlayer != null) {
            plugin.getMissionManager().updateMissionProgress(skyPlayer, 
                fr.skyblock.models.MissionType.BUILD_BLOCKS, event.getBlock().getType());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        if (!plugin.getWorldManager().isInSkyWorld(killer.getLocation())) {
            return;
        }
        
        SkyPlayer skyPlayer = plugin.getDatabaseManager().loadPlayer(killer.getUniqueId()).join();
        if (skyPlayer != null) {
            plugin.getMissionManager().updateMissionProgress(skyPlayer, 
                fr.skyblock.models.MissionType.KILL_MOBS, event.getEntity().getType());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCraftItem(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();
        
        SkyPlayer skyPlayer = plugin.getDatabaseManager().loadPlayer(player.getUniqueId()).join();
        if (skyPlayer != null) {
            plugin.getMissionManager().onItemCraft(skyPlayer, result.getType());
        }
    }
    
    // Note: PlayerPickupItemEvent est deprecated, utilisation d'EntityPickupItemEvent pour les versions récentes
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        if (!plugin.getWorldManager().isInSkyWorld(player.getLocation())) {
            return;
        }
        
        SkyPlayer skyPlayer = plugin.getDatabaseManager().loadPlayer(player.getUniqueId()).join();
        if (skyPlayer != null) {
            plugin.getMissionManager().onItemCollect(skyPlayer, event.getItem().getItemStack().getType());
        }
    }
}
package fr.skyblock.listeners;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.Island;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Block;
import org.bukkit.block.Container;

public class ProtectionListener implements Listener {
    
    private final SkyBlockPlugin plugin;
    
    public ProtectionListener(SkyBlockPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        if (!plugin.getWorldManager().isInSkyWorld(block.getLocation())) {
            return;
        }
        
        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        
        if (island == null) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous ne pouvez pas casser de blocs ici !");
            return;
        }
        
        if (!island.canBreak(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous n'avez pas la permission de casser des blocs sur cette île !");
            return;
        }
        
        island.updateActivity();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        if (!plugin.getWorldManager().isInSkyWorld(block.getLocation())) {
            return;
        }
        
        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        
        if (island == null) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous ne pouvez pas poser de blocs ici !");
            return;
        }
        
        if (!island.canBuild(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous n'avez pas la permission de poser des blocs sur cette île !");
            return;
        }
        
        island.updateActivity();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null || !plugin.getWorldManager().isInSkyWorld(block.getLocation())) {
            return;
        }
        
        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        
        if (island == null) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous ne pouvez pas interagir ici !");
            return;
        }
        
        if (!island.canInteract(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous n'avez pas la permission d'interagir sur cette île !");
            return;
        }
        
        island.updateActivity();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (!(holder instanceof Container)) {
            return;
        }
        
        Container container = (Container) holder;
        
        if (!plugin.getWorldManager().isInSkyWorld(container.getLocation())) {
            return;
        }
        
        Island island = plugin.getIslandManager().getIslandAt(container.getLocation());
        
        if (island == null) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous ne pouvez pas ouvrir de contenants ici !");
            return;
        }
        
        if (!island.canInteract(player.getUniqueId()) || !island.getSetting("allow_chest_access")) {
            event.setCancelled(true);
            player.sendMessage("§c❌ Vous n'avez pas la permission d'ouvrir les contenants sur cette île !");
            return;
        }
        
        island.updateActivity();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();
        
        if (!(damager instanceof Player) || !(target instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) damager;
        Player victim = (Player) target;
        
        if (!plugin.getWorldManager().isInSkyWorld(victim.getLocation())) {
            return;
        }
        
        Island island = plugin.getIslandManager().getIslandAt(victim.getLocation());
        
        if (island == null) {
            event.setCancelled(true);
            attacker.sendMessage("§c❌ Vous ne pouvez pas attaquer ici !");
            return;
        }
        
        if (!island.isPvpEnabled()) {
            event.setCancelled(true);
            attacker.sendMessage("§c❌ Le PvP est désactivé sur cette île !");
            return;
        }
        
        if (island.isMember(attacker.getUniqueId()) && island.isMember(victim.getUniqueId())) {
            if (!island.getSetting("allow_pvp_between_members")) {
                event.setCancelled(true);
                attacker.sendMessage("§c❌ Vous ne pouvez pas attaquer les membres de l'île !");
                return;
            }
        }
        
        island.updateActivity();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }
        
        if (!plugin.getWorldManager().isInSkyWorld(event.getTo())) {
            return;
        }
        
        Island oldIsland = plugin.getIslandManager().getIslandAt(event.getFrom());
        Island newIsland = plugin.getIslandManager().getIslandAt(event.getTo());
        
        if (oldIsland != newIsland) {
            if (oldIsland != null) {
                sendIslandLeaveMessage(player, oldIsland);
            }
            
            if (newIsland != null) {
                if (newIsland.isBanned(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage("§c❌ Vous êtes banni de cette île !");
                    return;
                }
                
                sendIslandEnterMessage(player, newIsland);
                newIsland.updateActivity();
            }
        }
    }
    
    private void sendIslandEnterMessage(Player player, Island island) {
        if (island.isMember(player.getUniqueId())) {
            player.sendMessage("§a🏠 Bienvenue chez vous !");
        } else {
            player.sendMessage("§e🏝️ Vous visitez l'île de §f" + getOwnerName(island));
            if (!island.getSetting("allow_visitors")) {
                player.sendMessage("§c⚠️ Cette île n'autorise pas les visiteurs !");
            }
        }
    }
    
    private void sendIslandLeaveMessage(Player player, Island island) {
        if (!island.isMember(player.getUniqueId())) {
            player.sendMessage("§7👋 Vous quittez l'île de §f" + getOwnerName(island));
        }
    }
    
    private String getOwnerName(Island island) {
        Player owner = plugin.getServer().getPlayer(island.getOwnerId());
        return owner != null ? owner.getName() : "Inconnu";
    }
}
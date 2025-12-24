package fr.skyblock.gui;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.models.Island;
import fr.skyblock.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TopIslandsGUI implements Listener {
    
    private final SkyBlockPlugin plugin;
    private final String title = "§6§l🏆 Classement des Îles";
    
    public TopIslandsGUI(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        List<Island> topIslands = plugin.getIslandManager().getTopIslands(10);
        
        // Titre du classement
        ItemStack titleItem = new ItemBuilder(Material.GOLDEN_APPLE)
                .setName("§6§l👑 TOP 10 des Îles")
                .setLore("§7Classement basé sur le niveau",
                        "§7des îles et leur développement",
                        "",
                        "§eTotal d'îles: §f" + plugin.getIslandManager().getTotalIslands())
                .setGlowing(true)
                .build();
        gui.setItem(4, titleItem);
        
        // Afficher les îles du top 10
        int[] slots = {10, 12, 14, 16, 19, 21, 23, 25, 28, 30};
        
        for (int i = 0; i < Math.min(topIslands.size(), 10); i++) {
            Island island = topIslands.get(i);
            Player owner = Bukkit.getPlayer(island.getOwnerId());
            String ownerName = owner != null ? owner.getName() : "Joueur inconnu";
            
            Material material = getMaterialForRank(i + 1);
            String rankColor = getColorForRank(i + 1);
            
            ItemStack islandItem = new ItemBuilder(material)
                    .setName(rankColor + "§l#" + (i + 1) + " " + island.getName())
                    .setLore("§7Propriétaire: §f" + ownerName,
                            "§7Niveau: §6" + island.getLevel(),
                            "§7Taille: §b" + island.getSize() + "x" + island.getSize(),
                            "§7Biome: §a" + island.getBiome(),
                            "§7Membres: §e" + island.getMembers().size(),
                            "§7PvP: " + (island.isPvpEnabled() ? "§aActivé" : "§cDésactivé"),
                            "",
                            "§e» Clic pour visiter l'île")
                    .build();
            
            if (i < 3) { // Top 3 avec effet brillant
                islandItem = new ItemBuilder(islandItem).setGlowing(true).build();
            }
            
            gui.setItem(slots[i], islandItem);
        }
        
        // Statistiques personnelles
        Island playerIsland = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        ItemStack playerStats;
        
        if (playerIsland != null) {
            int playerRank = getPlayerRank(player, topIslands);
            String rankText = playerRank > 0 ? "#" + playerRank : "Non classé";
            
            playerStats = new ItemBuilder(Material.PLAYER_HEAD)
                    .setSkullOwner(player.getName())
                    .setName("§b§l📊 Votre Position")
                    .setLore("§7Votre île: §f" + playerIsland.getName(),
                            "§7Classement: §6" + rankText,
                            "§7Niveau: §e" + playerIsland.getLevel(),
                            "§7Taille: §b" + playerIsland.getSize() + "x" + playerIsland.getSize(),
                            "§7Membres: §a" + playerIsland.getMembers().size(),
                            "",
                            "§7💡 Améliorez votre île pour",
                            "§7monter dans le classement !")
                    .build();
        } else {
            playerStats = new ItemBuilder(Material.BARRIER)
                    .setName("§c§l❌ Pas d'Île")
                    .setLore("§7Vous n'avez pas encore d'île !",
                            "",
                            "§e» Utilisez §f/island §epour en créer une")
                    .build();
        }
        gui.setItem(49, playerStats);
        
        // Informations sur le classement
        ItemStack info = new ItemBuilder(Material.BOOK)
                .setName("§f§lℹ️ Comment gravir le classement ?")
                .setLore("§7Le niveau de votre île dépend de:",
                        "",
                        "§e• §7Nombre et variété de blocs",
                        "§e• §7Constructions et décorations", 
                        "§e• §7Animaux et cultures",
                        "§e• §7Activité sur l'île",
                        "§e• §7Nombre de membres actifs",
                        "",
                        "§6💡 Conseil: Diversifiez votre île !")
                .build();
        gui.setItem(8, info);
        
        // Bouton actualiser
        ItemStack refresh = new ItemBuilder(Material.CLOCK)
                .setName("§a§l🔄 Actualiser")
                .setLore("§7Actualiser le classement",
                        "",
                        "§e» Clic pour actualiser")
                .build();
        gui.setItem(0, refresh);
        
        // Retour
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("§c§l← Retour")
                .setLore("§7Retourner au menu principal")
                .build();
        gui.setItem(45, backItem);
        
        // Décoration
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }
    
    private Material getMaterialForRank(int rank) {
        return switch (rank) {
            case 1 -> Material.GOLDEN_APPLE;
            case 2 -> Material.GOLDEN_CARROT;
            case 3 -> Material.GOLD_INGOT;
            case 4, 5 -> Material.IRON_INGOT;
            case 6, 7, 8 -> Material.COPPER_INGOT;
            default -> Material.COBBLESTONE;
        };
    }
    
    private String getColorForRank(int rank) {
        return switch (rank) {
            case 1 -> "§6"; // Or
            case 2 -> "§f"; // Argent
            case 3 -> "§c"; // Bronze
            case 4, 5 -> "§7"; // Fer
            default -> "§8"; // Pierre
        };
    }
    
    private int getPlayerRank(Player player, List<Island> topIslands) {
        Island playerIsland = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (playerIsland == null) return -1;
        
        for (int i = 0; i < topIslands.size(); i++) {
            if (topIslands.get(i).getId().equals(playerIsland.getId())) {
                return i + 1;
            }
        }
        return -1;
    }
    
    private void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName("§7")
                .build();
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Actualiser
                player.closeInventory();
                openGUI(player);
                player.sendMessage("§a✅ Classement actualisé !");
                break;
            case 45: // Retour
                new IslandMainGUI(plugin).openGUI(player);
                break;
            default:
                // Vérifier si c'est un slot d'île du top
                int[] topSlots = {10, 12, 14, 16, 19, 21, 23, 25, 28, 30};
                for (int i = 0; i < topSlots.length; i++) {
                    if (slot == topSlots[i]) {
                        visitIsland(player, i + 1);
                        break;
                    }
                }
                break;
        }
    }
    
    private void visitIsland(Player player, int rank) {
        List<Island> topIslands = plugin.getIslandManager().getTopIslands(10);
        
        if (rank <= topIslands.size()) {
            Island island = topIslands.get(rank - 1);
            Player owner = Bukkit.getPlayer(island.getOwnerId());
            String ownerName = owner != null ? owner.getName() : "Joueur inconnu";
            
            if (island.getSetting("allow_visitors")) {
                plugin.getIslandManager().teleportToIsland(player, island);
                player.sendMessage("§a🌟 Téléportation vers l'île #" + rank + " de " + ownerName + " !");
                player.closeInventory();
            } else {
                player.sendMessage("§c❌ Cette île n'autorise pas les visiteurs !");
            }
        }
    }
}
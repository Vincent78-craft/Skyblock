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

public class IslandCreationGUI implements Listener {
    
    private final SkyBlockPlugin plugin;
    private final String title = "§6§l🌟 Créer votre Île";
    
    public IslandCreationGUI(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, title);
        
        // Île Classique
        ItemStack classicIsland = new ItemBuilder(Material.GRASS_BLOCK)
                .setName("§a§l🌱 Île Classique")
                .setLore("§7Une île traditionnelle avec:",
                        "§7• De l'herbe et de la terre",
                        "§7• Un chêne au centre", 
                        "§7• Un coffre avec des objets de base",
                        "",
                        "§a✅ Recommandée pour débuter",
                        "",
                        "§e» Clic pour choisir ce schéma")
                .build();
        gui.setItem(11, classicIsland);
        
        // Île Désert
        ItemStack desertIsland = new ItemBuilder(Material.SAND)
                .setName("§e§l🏜️ Île Désert")
                .setLore("§7Une île aride avec:",
                        "§7• Du sable et du grès",
                        "§7• Des cactus",
                        "§7• Un point d'eau avec canne à sucre",
                        "",
                        "§6⚠️ Plus difficile",
                        "",
                        "§e» Clic pour choisir ce schéma")
                .build();
        gui.setItem(13, desertIsland);
        
        // Île Neige
        ItemStack snowIsland = new ItemBuilder(Material.SNOW_BLOCK)
                .setName("§b§l❄️ Île Enneigée")
                .setLore("§7Une île glaciale avec:",
                        "§7• De la neige partout",
                        "§7• Un sapin",
                        "§7• Climat froid permanent",
                        "",
                        "§9❄️ Défi pour les experts",
                        "",
                        "§e» Clic pour choisir ce schéma")
                .build();
        gui.setItem(15, snowIsland);
        
        // Retour
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("§c§l← Retour")
                .setLore("§7Retourner au menu principal")
                .build();
        gui.setItem(22, backItem);
        
        // Décoration
        fillEmptySlots(gui);
        
        player.openInventory(gui);
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
            case 11: // Île Classique
                createIsland(player, "classic");
                break;
            case 13: // Île Désert
                createIsland(player, "desert");
                break;
            case 15: // Île Neige
                createIsland(player, "snow");
                break;
            case 22: // Retour
                new IslandMainGUI(plugin).openGUI(player);
                break;
        }
    }
    
    private void createIsland(Player player, String schematic) {
        player.closeInventory();
        
        // Vérifier si le joueur a déjà une île
        if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
            player.sendMessage("§c❌ Vous avez déjà une île !");
            return;
        }
        
        player.sendMessage("§e⏳ Création de votre île en cours...");
        
        // Créer l'île sur le thread principal
        Bukkit.getScheduler().runTask(plugin, () -> {
            Island island = plugin.getIslandManager().createIsland(player, schematic);
            
            if (island != null) {
                plugin.getIslandManager().teleportToIsland(player, island);
                
                player.sendMessage("§a✅ Votre île a été créée avec succès !");
                player.sendMessage("§e🏠 Bienvenue sur votre nouvelle île !");
                player.sendMessage("§7💡 Utilisez §f/island §7pour accéder aux menus.");
                
                // Donner des SkyCoins de bienvenue
                plugin.getEconomyManager().addSkyCoins(player.getUniqueId(), 100);
                player.sendMessage("§6💰 Vous avez reçu 100 SkyCoins de bienvenue !");
            } else {
                player.sendMessage("§c❌ Erreur lors de la création de l'île !");
            }
        });
    }
}
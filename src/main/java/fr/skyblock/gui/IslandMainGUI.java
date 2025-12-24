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

public class IslandMainGUI implements Listener {
    
    private final SkyBlockPlugin plugin;
    private final String title = "§6§l🏝️ SkyBlock Menu";
    
    public IslandMainGUI(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, title);
        
        Island playerIsland = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        boolean hasIsland = playerIsland != null;
        
        // Créer/Rejoindre île
        ItemStack createIslandItem;
        if (hasIsland) {
            createIslandItem = new ItemBuilder(Material.GRASS_BLOCK)
                    .setName("§a§l🏠 Mon Île")
                    .setLore("§7Gérer votre île",
                            "§7Nom: §f" + playerIsland.getName(),
                            "§7Niveau: §6" + playerIsland.getLevel(),
                            "§7Membres: §b" + playerIsland.getMembers().size(),
                            "",
                            "§e» Clic pour gérer")
                    .build();
        } else {
            createIslandItem = new ItemBuilder(Material.GRASS_BLOCK)
                    .setName("§a§l🌟 Créer mon Île")
                    .setLore("§7Créer votre première île",
                            "§7Choisissez un schéma et",
                            "§7commencez votre aventure !",
                            "",
                            "§e» Clic pour créer")
                    .build();
        }
        gui.setItem(10, createIslandItem);
        
        // Téléportation
        if (hasIsland) {
            ItemStack teleportItem = new ItemBuilder(Material.ENDER_PEARL)
                    .setName("§b§l🚀 Aller à mon Île")
                    .setLore("§7Se téléporter à votre île",
                            "",
                            "§e» Clic pour se téléporter")
                    .build();
            gui.setItem(12, teleportItem);
        }
        
        // Missions
        ItemStack missionsItem = new ItemBuilder(Material.BOOK)
                .setName("§e§l📜 Missions")
                .setLore("§7Voir les missions disponibles",
                        "§7Gagnez des SkyCoins et",
                        "§7des récompenses !",
                        "",
                        "§e» Clic pour voir les missions")
                .build();
        gui.setItem(14, missionsItem);
        
        // Shop
        ItemStack shopItem = new ItemBuilder(Material.EMERALD)
                .setName("§2§l🛒 Shop SkyBlock")
                .setLore("§7Acheter des objets et",
                        "§7des améliorations",
                        "§7Vos SkyCoins: §6" + plugin.getEconomyManager().getSkyCoins(player.getUniqueId()),
                        "",
                        "§e» Clic pour ouvrir le shop")
                .build();
        gui.setItem(16, shopItem);
        
        // Classement
        ItemStack topItem = new ItemBuilder(Material.GOLDEN_APPLE)
                .setName("§6§l🏆 Classement")
                .setLore("§7Voir le classement des îles",
                        "§7les plus développées",
                        "",
                        "§e» Clic pour voir le top")
                .build();
        gui.setItem(22, topItem);
        
        // Paramètres (si le joueur a une île)
        if (hasIsland) {
            ItemStack settingsItem = new ItemBuilder(Material.REDSTONE)
                    .setName("§c§l⚙️ Paramètres d'Île")
                    .setLore("§7Configurer votre île",
                            "§7Gérer les membres, permissions,",
                            "§7biome, PvP, etc.",
                            "",
                            "§e» Clic pour configurer")
                    .build();
            gui.setItem(20, settingsItem);
        }
        
        // Décoration du GUI
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
            case 10: // Créer/Gérer île
                handleIslandAction(player);
                break;
            case 12: // Téléporter
                handleTeleport(player);
                break;
            case 14: // Missions
                new MissionsGUI(plugin).openGUI(player);
                break;
            case 16: // Shop
                new ShopGUI(plugin).openGUI(player);
                break;
            case 20: // Paramètres
                new IslandSettingsGUI(plugin).openGUI(player);
                break;
            case 22: // Classement
                new TopIslandsGUI(plugin).openGUI(player);
                break;
        }
    }
    
    private void handleIslandAction(Player player) {
        Island playerIsland = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        
        if (playerIsland == null) {
            // Ouvrir le GUI de création d'île
            new IslandCreationGUI(plugin).openGUI(player);
        } else {
            // Ouvrir le GUI de gestion d'île
            new IslandManagementGUI(plugin).openGUI(player);
        }
    }
    
    private void handleTeleport(Player player) {
        Island playerIsland = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        
        if (playerIsland == null) {
            player.sendMessage("§c❌ Vous n'avez pas d'île !");
            return;
        }
        
        plugin.getIslandManager().teleportToIsland(player, playerIsland);
        player.sendMessage("§a✅ Téléportation vers votre île !");
        player.closeInventory();
    }
}
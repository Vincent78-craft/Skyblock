package fr.skyblock.gui;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopGUI implements Listener {
    
    private final SkyBlockPlugin plugin;
    private final String title = "§2§l🛒 Shop SkyBlock";
    
    public ShopGUI(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        int playerCoins = plugin.getEconomyManager().getSkyCoins(player.getUniqueId());
        
        // Catégorie Blocs de base
        ItemStack basicBlocks = new ItemBuilder(Material.COBBLESTONE)
                .setName("§8§l🧱 Blocs de Base")
                .setLore("§7Blocs essentiels pour construire",
                        "§7votre île et vos structures",
                        "",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir les blocs")
                .build();
        gui.setItem(10, basicBlocks);
        
        // Catégorie Ressources
        ItemStack resources = new ItemBuilder(Material.IRON_INGOT)
                .setName("§7§l⛏️ Ressources")
                .setLore("§7Minerais et matériaux",
                        "§7pour crafter et progresser",
                        "",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir les ressources")
                .build();
        gui.setItem(12, resources);
        
        // Catégorie Nourriture
        ItemStack food = new ItemBuilder(Material.BREAD)
                .setName("§6§l🍞 Nourriture")
                .setLore("§7Aliments pour survivre",
                        "§7et restaurer votre santé",
                        "",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir la nourriture")
                .build();
        gui.setItem(14, food);
        
        // Catégorie Outils
        ItemStack tools = new ItemBuilder(Material.DIAMOND_PICKAXE)
                .setName("§b§l🛠️ Outils")
                .setLore("§7Outils et équipements",
                        "§7pour améliorer votre efficacité",
                        "",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir les outils")
                .build();
        gui.setItem(16, tools);
        
        // Catégorie Améliorations
        ItemStack upgrades = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§d§l✨ Améliorations")
                .setLore("§7Améliorations pour votre île",
                        "§7Augmentez la taille, les membres, etc.",
                        "",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir les améliorations")
                .build();
        gui.setItem(28, upgrades);
        
        // Catégorie Spawners
        ItemStack spawners = new ItemBuilder(Material.SPAWNER)
                .setName("§5§l🔮 Générateurs")
                .setLore("§7Générateurs de mobs",
                        "§7pour l'xp et les drops",
                        "",
                        "§c💰 Prix élevés !",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir les générateurs")
                .build();
        gui.setItem(30, spawners);
        
        // Catégorie Décoration
        ItemStack decoration = new ItemBuilder(Material.FLOWER_POT)
                .setName("§a§l🌸 Décoration")
                .setLore("§7Objets décoratifs",
                        "§7pour embellir votre île",
                        "",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir les décorations")
                .build();
        gui.setItem(32, decoration);
        
        // Catégorie Spécial
        ItemStack special = new ItemBuilder(Material.NETHER_STAR)
                .setName("§e§l⭐ Objets Spéciaux")
                .setLore("§7Objets rares et puissants",
                        "§7pour les joueurs expérimentés",
                        "",
                        "§c💎 Très coûteux !",
                        "§6Vos SkyCoins: §e" + playerCoins,
                        "",
                        "§e» Clic pour voir les objets spéciaux")
                .build();
        gui.setItem(34, special);
        
        // Retour
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("§c§l← Retour")
                .setLore("§7Retourner au menu principal")
                .build();
        gui.setItem(49, backItem);
        
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
            case 10: // Blocs de base
                openCategoryGUI(player, "basic_blocks");
                break;
            case 12: // Ressources
                openCategoryGUI(player, "resources");
                break;
            case 14: // Nourriture
                openCategoryGUI(player, "food");
                break;
            case 16: // Outils
                openCategoryGUI(player, "tools");
                break;
            case 28: // Améliorations
                openCategoryGUI(player, "upgrades");
                break;
            case 30: // Spawners
                openCategoryGUI(player, "spawners");
                break;
            case 32: // Décoration
                openCategoryGUI(player, "decoration");
                break;
            case 34: // Spécial
                openCategoryGUI(player, "special");
                break;
            case 49: // Retour
                new IslandMainGUI(plugin).openGUI(player);
                break;
        }
    }
    
    private void openCategoryGUI(Player player, String category) {
        player.closeInventory();
        player.sendMessage("§e🛒 Ouverture de la catégorie " + category + "...");
        player.sendMessage("§7💡 Cette fonctionnalité sera bientôt disponible !");
        
        // TODO: Implémenter les GUI de catégories spécifiques
    }
}
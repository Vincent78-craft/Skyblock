package fr.skyblock.managers;

import fr.skyblock.SkyBlockPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShopManager {
    
    private final SkyBlockPlugin plugin;
    private final Map<String, ShopCategory> categories;
    
    public ShopManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        this.categories = new HashMap<>();
        
        initializeDefaultShop();
    }
    
    private void initializeDefaultShop() {
        // Catégorie Blocs de base
        ShopCategory basicBlocks = new ShopCategory("basic_blocks", "§8§l🧱 Blocs de Base");
        basicBlocks.addItem(new ShopItem(Material.COBBLESTONE, "Cobblestone", 1, 64));
        basicBlocks.addItem(new ShopItem(Material.STONE, "Pierre", 2, 64));
        basicBlocks.addItem(new ShopItem(Material.DIRT, "Terre", 1, 64));
        basicBlocks.addItem(new ShopItem(Material.SAND, "Sable", 2, 64));
        basicBlocks.addItem(new ShopItem(Material.GRAVEL, "Gravier", 2, 64));
        basicBlocks.addItem(new ShopItem(Material.OAK_PLANKS, "Planches de Chêne", 3, 64));
        categories.put("basic_blocks", basicBlocks);
        
        // Catégorie Ressources
        ShopCategory resources = new ShopCategory("resources", "§7§l⛏️ Ressources");
        resources.addItem(new ShopItem(Material.COAL, "Charbon", 5, 64));
        resources.addItem(new ShopItem(Material.IRON_INGOT, "Lingot de Fer", 10, 32));
        resources.addItem(new ShopItem(Material.GOLD_INGOT, "Lingot d'Or", 20, 16));
        resources.addItem(new ShopItem(Material.DIAMOND, "Diamant", 100, 8));
        resources.addItem(new ShopItem(Material.EMERALD, "Émeraude", 150, 4));
        resources.addItem(new ShopItem(Material.REDSTONE, "Redstone", 8, 64));
        categories.put("resources", resources);
        
        // Catégorie Nourriture
        ShopCategory food = new ShopCategory("food", "§6§l🍞 Nourriture");
        food.addItem(new ShopItem(Material.BREAD, "Pain", 5, 32));
        food.addItem(new ShopItem(Material.COOKED_BEEF, "Bœuf Cuit", 8, 16));
        food.addItem(new ShopItem(Material.COOKED_PORKCHOP, "Porc Cuit", 8, 16));
        food.addItem(new ShopItem(Material.GOLDEN_APPLE, "Pomme Dorée", 50, 4));
        food.addItem(new ShopItem(Material.CAKE, "Gâteau", 25, 1));
        categories.put("food", food);
        
        // Catégorie Outils
        ShopCategory tools = new ShopCategory("tools", "§b§l🛠️ Outils");
        tools.addItem(new ShopItem(Material.WOODEN_PICKAXE, "Pioche en Bois", 10, 1));
        tools.addItem(new ShopItem(Material.STONE_PICKAXE, "Pioche en Pierre", 25, 1));
        tools.addItem(new ShopItem(Material.IRON_PICKAXE, "Pioche en Fer", 50, 1));
        tools.addItem(new ShopItem(Material.DIAMOND_PICKAXE, "Pioche en Diamant", 500, 1));
        tools.addItem(new ShopItem(Material.WOODEN_AXE, "Hache en Bois", 10, 1));
        tools.addItem(new ShopItem(Material.STONE_AXE, "Hache en Pierre", 25, 1));
        categories.put("tools", tools);
        
        // Catégorie Spawners (très chers)
        ShopCategory spawners = new ShopCategory("spawners", "§5§l🔮 Générateurs");
        spawners.addItem(new ShopItem(Material.SPAWNER, "Générateur de Zombies", 5000, 1));
        spawners.addItem(new ShopItem(Material.SPAWNER, "Générateur de Squelettes", 6000, 1));
        spawners.addItem(new ShopItem(Material.SPAWNER, "Générateur de Creepers", 8000, 1));
        spawners.addItem(new ShopItem(Material.SPAWNER, "Générateur d'Endermen", 15000, 1));
        categories.put("spawners", spawners);
        
        // Catégorie Décoration
        ShopCategory decoration = new ShopCategory("decoration", "§a§l🌸 Décoration");
        decoration.addItem(new ShopItem(Material.FLOWER_POT, "Pot de Fleur", 15, 8));
        decoration.addItem(new ShopItem(Material.PAINTING, "Peinture", 20, 4));
        decoration.addItem(new ShopItem(Material.ITEM_FRAME, "Cadre", 10, 8));
        decoration.addItem(new ShopItem(Material.BEACON, "Balise", 2000, 1));
        categories.put("decoration", decoration);
        
        plugin.getLogger().info("Shop par défaut initialisé : " + categories.size() + " catégories");
    }
    
    public boolean buyItem(Player player, String categoryId, Material material, int quantity) {
        ShopCategory category = categories.get(categoryId);
        if (category == null) {
            return false;
        }
        
        ShopItem shopItem = category.getItem(material);
        if (shopItem == null) {
            return false;
        }
        
        int totalCost = shopItem.getPrice() * quantity;
        
        if (!plugin.getEconomyManager().hasEnoughSkyCoins(player.getUniqueId(), totalCost)) {
            player.sendMessage("§c❌ SkyCoins insuffisants ! Il vous faut " + totalCost + " SkyCoins.");
            return false;
        }
        
        // Vérifier l'espace dans l'inventaire
        ItemStack item = new ItemStack(material, quantity);
        if (!hasInventorySpace(player, item)) {
            player.sendMessage("§c❌ Votre inventaire est plein !");
            return false;
        }
        
        // Effectuer la transaction
        if (plugin.getEconomyManager().removeSkyCoins(player.getUniqueId(), totalCost)) {
            player.getInventory().addItem(item);
            player.sendMessage("§a✅ Achat réussi ! §7(-" + totalCost + " SkyCoins)");
            
            // Tracking pour les missions
            var skyPlayer = plugin.getDatabaseManager().loadPlayer(player.getUniqueId()).join();
            if (skyPlayer != null) {
                plugin.getMissionManager().onCoinsSpent(skyPlayer, totalCost);
            }
            
            return true;
        }
        
        return false;
    }
    
    private boolean hasInventorySpace(Player player, ItemStack item) {
        int needed = item.getAmount();
        
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null) {
                needed -= item.getMaxStackSize();
            } else if (invItem.isSimilar(item)) {
                needed -= (item.getMaxStackSize() - invItem.getAmount());
            }
            
            if (needed <= 0) {
                return true;
            }
        }
        
        return false;
    }
    
    public ShopCategory getCategory(String categoryId) {
        return categories.get(categoryId);
    }
    
    public Map<String, ShopCategory> getAllCategories() {
        return new HashMap<>(categories);
    }
    
    public void reloadShop() {
        categories.clear();
        initializeDefaultShop();
        // TODO: Charger depuis la configuration
        plugin.getLogger().info("Shop rechargé !");
    }
    
    // Classes internes pour le shop
    public static class ShopCategory {
        private final String id;
        private final String displayName;
        private final Map<Material, ShopItem> items;
        
        public ShopCategory(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
            this.items = new HashMap<>();
        }
        
        public void addItem(ShopItem item) {
            items.put(item.getMaterial(), item);
        }
        
        public ShopItem getItem(Material material) {
            return items.get(material);
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Map<Material, ShopItem> getItems() { return new HashMap<>(items); }
    }
    
    public static class ShopItem {
        private final Material material;
        private final String displayName;
        private final int price;
        private final int maxQuantity;
        private boolean enabled;
        
        public ShopItem(Material material, String displayName, int price, int maxQuantity) {
            this.material = material;
            this.displayName = displayName;
            this.price = price;
            this.maxQuantity = maxQuantity;
            this.enabled = true;
        }
        
        public Material getMaterial() { return material; }
        public String getDisplayName() { return displayName; }
        public int getPrice() { return price; }
        public int getMaxQuantity() { return maxQuantity; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
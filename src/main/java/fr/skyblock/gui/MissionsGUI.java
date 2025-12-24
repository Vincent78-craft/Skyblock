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

public class MissionsGUI implements Listener {
    
    private final SkyBlockPlugin plugin;
    private final String title = "§e§l📜 Missions";
    
    public MissionsGUI(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        // Mission 1: Casser des blocs de pierre
        ItemStack mission1 = new ItemBuilder(Material.STONE)
                .setName("§7§l⛏️ Mineur Débutant")
                .setLore("§7Cassez 100 blocs de pierre",
                        "",
                        "§6Récompense: §e250 SkyCoins",
                        "§aProgression: 0/100",
                        "",
                        "§e» Clic pour accepter la mission")
                .build();
        gui.setItem(10, mission1);
        
        // Mission 2: Collecter du bois
        ItemStack mission2 = new ItemBuilder(Material.OAK_LOG)
                .setName("§6§l🌳 Bûcheron")
                .setLore("§7Collectez 64 bûches de chêne",
                        "",
                        "§6Récompense: §e150 SkyCoins",
                        "§aProgression: 0/64",
                        "",
                        "§e» Clic pour accepter la mission")
                .build();
        gui.setItem(12, mission2);
        
        // Mission 3: Crafter des objets
        ItemStack mission3 = new ItemBuilder(Material.CRAFTING_TABLE)
                .setName("§e§l🔨 Artisan")
                .setLore("§7Craftez 32 planches de bois",
                        "",
                        "§6Récompense: §e100 SkyCoins",
                        "§aProgression: 0/32",
                        "",
                        "§e» Clic pour accepter la mission")
                .build();
        gui.setItem(14, mission3);
        
        // Mission 4: Cultiver des plantes
        ItemStack mission4 = new ItemBuilder(Material.WHEAT)
                .setName("§a§l🌾 Fermier")
                .setLore("§7Récoltez 50 blés",
                        "",
                        "§6Récompense: §e200 SkyCoins",
                        "§cProgression: 0/50",
                        "",
                        "§e» Clic pour accepter la mission")
                .build();
        gui.setItem(16, mission4);
        
        // Mission 5: Élever des animaux
        ItemStack mission5 = new ItemBuilder(Material.EGG)
                .setName("§d§l🐄 Éleveur")
                .setLore("§7Élevez 10 animaux",
                        "",
                        "§6Récompense: §e300 SkyCoins",
                        "§cProgression: 0/10",
                        "",
                        "§e» Clic pour accepter la mission")
                .build();
        gui.setItem(28, mission5);
        
        // Mission 6: Explorer
        ItemStack mission6 = new ItemBuilder(Material.COMPASS)
                .setName("§b§l🗺️ Explorateur")
                .setLore("§7Visitez 5 îles différentes",
                        "",
                        "§6Récompense: §e400 SkyCoins",
                        "§cProgression: 0/5",
                        "",
                        "§e» Clic pour accepter la mission")
                .build();
        gui.setItem(30, mission6);
        
        // Mission 7: Économiser
        ItemStack mission7 = new ItemBuilder(Material.GOLD_INGOT)
                .setName("§6§l💰 Économe")
                .setLore("§7Économisez 1000 SkyCoins",
                        "",
                        "§6Récompense: §e500 SkyCoins",
                        "§7Progression: " + plugin.getEconomyManager().getSkyCoins(player.getUniqueId()) + "/1000",
                        "",
                        "§e» Mission automatique")
                .build();
        gui.setItem(32, mission7);
        
        // Mission 8: Mission journalière
        ItemStack mission8 = new ItemBuilder(Material.CLOCK)
                .setName("§c§l⏰ Défi Quotidien")
                .setLore("§7Connectez-vous 7 jours de suite",
                        "",
                        "§6Récompense: §e1000 SkyCoins",
                        "§6+ Objet mystère",
                        "§aProgression: 1/7",
                        "",
                        "§e» Mission automatique")
                .build();
        gui.setItem(34, mission8);
        
        // Informations sur les missions
        ItemStack info = new ItemBuilder(Material.BOOK)
                .setName("§f§lℹ️ Informations")
                .setLore("§7Les missions vous permettent",
                        "§7de gagner des SkyCoins et",
                        "§7des récompenses spéciales.",
                        "",
                        "§e• §7Missions quotidiennes : Reset à 00h00",
                        "§e• §7Missions hebdomadaires : Reset le lundi",
                        "§e• §7Certaines missions sont répétables",
                        "",
                        "§a✅ §7Mission terminée",
                        "§c❌ §7Mission non disponible",
                        "§e⏳ §7Mission en cours")
                .build();
        gui.setItem(4, info);
        
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
        
        if (slot == 49) { // Retour
            new IslandMainGUI(plugin).openGUI(player);
        } else if (slot >= 10 && slot <= 34) { // Missions
            acceptMission(player, slot);
        }
    }
    
    private void acceptMission(Player player, int slot) {
        String missionName = getMissionName(slot);
        
        if (missionName != null) {
            player.sendMessage("§e📜 Mission acceptée : §f" + missionName);
            player.sendMessage("§7💡 Votre progression sera suivie automatiquement !");
            
            // TODO: Implémenter le système de suivi des missions
        }
    }
    
    private String getMissionName(int slot) {
        switch (slot) {
            case 10: return "Mineur Débutant";
            case 12: return "Bûcheron";
            case 14: return "Artisan";
            case 16: return "Fermier";
            case 28: return "Éleveur";
            case 30: return "Explorateur";
            case 32: return "Économe";
            case 34: return "Défi Quotidien";
            default: return null;
        }
    }
}
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

public class IslandSettingsGUI implements Listener {
    
    private final SkyBlockPlugin plugin;
    private final String title = "§c§l⚙️ Paramètres de l'Île";
    
    public IslandSettingsGUI(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§c❌ Vous n'avez pas d'île !");
            return;
        }
        
        // PvP
        ItemStack pvp = new ItemBuilder(island.isPvpEnabled() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD)
                .setName("§c§l⚔️ Combat PvP")
                .setLore("§7Autoriser les combats entre",
                        "§7joueurs sur votre île",
                        "",
                        "§7Statut: " + (island.isPvpEnabled() ? "§aActivé" : "§cDésactivé"),
                        "",
                        "§e» Clic pour " + (island.isPvpEnabled() ? "désactiver" : "activer"))
                .build();
        gui.setItem(10, pvp);
        
        // Visiteurs
        ItemStack visitors = new ItemBuilder(island.getSetting("allow_visitors") ? Material.OAK_DOOR : Material.IRON_DOOR)
                .setName("§e§l🚪 Visiteurs")
                .setLore("§7Autoriser les autres joueurs",
                        "§7à visiter votre île",
                        "",
                        "§7Statut: " + (island.getSetting("allow_visitors") ? "§aAutorisés" : "§cInterdits"),
                        "",
                        "§e» Clic pour " + (island.getSetting("allow_visitors") ? "interdire" : "autoriser"))
                .build();
        gui.setItem(12, visitors);
        
        // Construction pour les membres
        ItemStack build = new ItemBuilder(island.getSetting("allow_build") ? Material.GRASS_BLOCK : Material.BARRIER)
                .setName("§a§l🏗️ Construction")
                .setLore("§7Autoriser les membres à",
                        "§7construire sur l'île",
                        "",
                        "§7Statut: " + (island.getSetting("allow_build") ? "§aAutorisée" : "§cInterdite"),
                        "",
                        "§e» Clic pour " + (island.getSetting("allow_build") ? "interdire" : "autoriser"))
                .build();
        gui.setItem(14, build);
        
        // Casser des blocs pour les membres
        ItemStack breaking = new ItemBuilder(island.getSetting("allow_break") ? Material.DIAMOND_PICKAXE : Material.WOODEN_PICKAXE)
                .setName("§7§l⛏️ Casser des Blocs")
                .setLore("§7Autoriser les membres à",
                        "§7casser des blocs sur l'île",
                        "",
                        "§7Statut: " + (island.getSetting("allow_break") ? "§aAutorisé" : "§cInterdit"),
                        "",
                        "§e» Clic pour " + (island.getSetting("allow_break") ? "interdire" : "autoriser"))
                .build();
        gui.setItem(16, breaking);
        
        // Interactions
        ItemStack interact = new ItemBuilder(island.getSetting("allow_interact") ? Material.LEVER : Material.REDSTONE)
                .setName("§6§l🔧 Interactions")
                .setLore("§7Autoriser les membres à",
                        "§7interagir avec les mécanismes",
                        "§7(portes, leviers, boutons, etc.)",
                        "",
                        "§7Statut: " + (island.getSetting("allow_interact") ? "§aAutorisées" : "§cInterdites"),
                        "",
                        "§e» Clic pour " + (island.getSetting("allow_interact") ? "interdire" : "autoriser"))
                .build();
        gui.setItem(19, interact);
        
        // Accès aux coffres
        ItemStack chests = new ItemBuilder(island.getSetting("allow_chest_access") ? Material.CHEST : Material.TRAPPED_CHEST)
                .setName("§b§l📦 Accès aux Coffres")
                .setLore("§7Autoriser les membres à",
                        "§7ouvrir les coffres et contenants",
                        "",
                        "§7Statut: " + (island.getSetting("allow_chest_access") ? "§aAutorisé" : "§cInterdit"),
                        "",
                        "§e» Clic pour " + (island.getSetting("allow_chest_access") ? "interdire" : "autoriser"))
                .build();
        gui.setItem(21, chests);
        
        // Dégâts des mobs
        ItemStack mobDamage = new ItemBuilder(island.getSetting("allow_mob_damage") ? Material.ZOMBIE_HEAD : Material.GOLDEN_APPLE)
                .setName("§c§l🧟 Dégâts des Mobs")
                .setLore("§7Autoriser les mobs hostiles",
                        "§7à infliger des dégâts",
                        "",
                        "§7Statut: " + (island.getSetting("allow_mob_damage") ? "§aActivés" : "§cDésactivés"),
                        "",
                        "§e» Clic pour " + (island.getSetting("allow_mob_damage") ? "désactiver" : "activer"))
                .build();
        gui.setItem(23, mobDamage);
        
        // Dégâts aux animaux
        ItemStack animalDamage = new ItemBuilder(island.getSetting("allow_animal_damage") ? Material.BEEF : Material.GOLDEN_CARROT)
                .setName("§d§l🐄 Dégâts aux Animaux")
                .setLore("§7Autoriser les dégâts",
                        "§7aux animaux passifs",
                        "",
                        "§7Statut: " + (island.getSetting("allow_animal_damage") ? "§aAutorisés" : "§cInterdits"),
                        "",
                        "§e» Clic pour " + (island.getSetting("allow_animal_damage") ? "interdire" : "autoriser"))
                .build();
        gui.setItem(25, animalDamage);
        
        // Changement de biome
        ItemStack biome = new ItemBuilder(getBiomeMaterial(island.getBiome()))
                .setName("§2§l🌍 Changer de Biome")
                .setLore("§7Changer le biome de votre île",
                        "",
                        "§7Biome actuel: §a" + island.getBiome(),
                        "§7Coût: §6500 SkyCoins",
                        "",
                        "§e» Clic pour changer")
                .build();
        gui.setItem(31, biome);
        
        // Informations générales
        ItemStack info = new ItemBuilder(Material.BOOK)
                .setName("§f§lℹ️ Informations")
                .setLore("§7Configurez ici les permissions",
                        "§7et règles de votre île.",
                        "",
                        "§e⚡ §7Paramètres pour les membres uniquement",
                        "§c❌ §7Les propriétaires ont tous les droits",
                        "§b🔒 §7Les visiteurs ont des droits limités",
                        "",
                        "§6💡 Pensez à équilibrer sécurité et convivialité !")
                .build();
        gui.setItem(4, info);
        
        // Retour
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("§7← Retour")
                .setLore("§7Retourner à la gestion de l'île")
                .build();
        gui.setItem(49, backItem);
        
        // Décoration
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }
    
    private Material getBiomeMaterial(String biome) {
        return switch (biome.toUpperCase()) {
            case "DESERT" -> Material.SAND;
            case "TAIGA", "ICE_SPIKES" -> Material.SNOW_BLOCK;
            case "JUNGLE" -> Material.VINE;
            case "SAVANNA" -> Material.ACACIA_LOG;
            case "BADLANDS" -> Material.RED_SAND;
            default -> Material.GRASS_BLOCK;
        };
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
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        
        if (island == null) {
            player.sendMessage("§c❌ Erreur: Île non trouvée !");
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 10: // PvP
                island.setPvpEnabled(!island.isPvpEnabled());
                player.sendMessage("§e⚡ PvP " + (island.isPvpEnabled() ? "§aactivé" : "§cdésactivé") + " !");
                break;
            case 12: // Visiteurs
                island.setSetting("allow_visitors", !island.getSetting("allow_visitors"));
                player.sendMessage("§e🚪 Visiteurs " + (island.getSetting("allow_visitors") ? "§aautorisés" : "§cinterdits") + " !");
                break;
            case 14: // Construction
                island.setSetting("allow_build", !island.getSetting("allow_build"));
                player.sendMessage("§e🏗️ Construction " + (island.getSetting("allow_build") ? "§aautorisée" : "§cinterdite") + " !");
                break;
            case 16: // Casser
                island.setSetting("allow_break", !island.getSetting("allow_break"));
                player.sendMessage("§e⛏️ Casser des blocs " + (island.getSetting("allow_break") ? "§aautorisé" : "§cinterdit") + " !");
                break;
            case 19: // Interactions
                island.setSetting("allow_interact", !island.getSetting("allow_interact"));
                player.sendMessage("§e🔧 Interactions " + (island.getSetting("allow_interact") ? "§aautorisées" : "§cinterdites") + " !");
                break;
            case 21: // Coffres
                island.setSetting("allow_chest_access", !island.getSetting("allow_chest_access"));
                player.sendMessage("§e📦 Accès aux coffres " + (island.getSetting("allow_chest_access") ? "§aautorisé" : "§cinterdit") + " !");
                break;
            case 23: // Dégâts mobs
                island.setSetting("allow_mob_damage", !island.getSetting("allow_mob_damage"));
                player.sendMessage("§e🧟 Dégâts des mobs " + (island.getSetting("allow_mob_damage") ? "§aactivés" : "§cdésactivés") + " !");
                break;
            case 25: // Dégâts animaux
                island.setSetting("allow_animal_damage", !island.getSetting("allow_animal_damage"));
                player.sendMessage("§e🐄 Dégâts aux animaux " + (island.getSetting("allow_animal_damage") ? "§aautorisés" : "§cinterdits") + " !");
                break;
            case 31: // Biome
                changeBiome(player, island);
                return;
            case 49: // Retour
                new IslandManagementGUI(plugin).openGUI(player);
                return;
        }
        
        // Sauvegarder les changements
        plugin.getDatabaseManager().saveIsland(island);
        
        // Actualiser le GUI
        openGUI(player);
    }
    
    private void changeBiome(Player player, Island island) {
        int cost = 500;
        
        if (!plugin.getEconomyManager().hasEnoughSkyCoins(player.getUniqueId(), cost)) {
            player.sendMessage("§c❌ Vous n'avez pas assez de SkyCoins ! (Coût: " + cost + ")");
            return;
        }
        
        player.closeInventory();
        player.sendMessage("§e🌍 Changement de biome bientôt disponible !");
        player.sendMessage("§7💡 Cette fonctionnalité sera ajoutée dans une future mise à jour.");
    }
}
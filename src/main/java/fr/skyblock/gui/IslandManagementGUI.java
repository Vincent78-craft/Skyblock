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

public class IslandManagementGUI implements Listener {
    
    private final SkyBlockPlugin plugin;
    private final String title = "§a§l🏠 Gestion de l'Île";
    
    public IslandManagementGUI(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, title);
        
        var island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§c❌ Vous n'avez pas d'île !");
            return;
        }
        
        // Informations de l'île
        ItemStack info = new ItemBuilder(Material.BOOK)
                .setName("§e§l📋 Informations de l'Île")
                .setLore("§7Nom: §f" + island.getName(),
                        "§7Niveau: §6" + island.getLevel(),
                        "§7Taille: §b" + island.getSize() + "x" + island.getSize(),
                        "§7Biome: §a" + island.getBiome(),
                        "§7Membres: §e" + island.getMembers().size() + "/10",
                        "§7PvP: " + (island.isPvpEnabled() ? "§aActivé" : "§cDésactivé"),
                        "",
                        "§e» Clic pour renommer l'île")
                .build();
        gui.setItem(4, info);
        
        // Gérer les membres
        ItemStack members = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§b§l👥 Gérer les Membres")
                .setLore("§7Inviter, expulser ou gérer",
                        "§7les rôles des membres de votre île",
                        "",
                        "§7Membres actuels: §e" + island.getMembers().size(),
                        "",
                        "§e» Clic pour gérer les membres")
                .build();
        gui.setItem(10, members);
        
        // Paramètres de l'île
        ItemStack settings = new ItemBuilder(Material.REDSTONE)
                .setName("§c§l⚙️ Paramètres")
                .setLore("§7Configurer les permissions",
                        "§7et les règles de votre île",
                        "",
                        "§7• Permissions de construction",
                        "§7• Accès aux coffres",
                        "§7• PvP et visiteurs",
                        "",
                        "§e» Clic pour configurer")
                .build();
        gui.setItem(12, settings);
        
        // Améliorations
        ItemStack upgrades = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§d§l✨ Améliorations")
                .setLore("§7Améliorer votre île avec",
                        "§7des SkyCoins",
                        "",
                        "§7• Augmenter la taille",
                        "§7• Plus de membres",
                        "§7• Nouveaux biomes",
                        "§7• Générateurs spéciaux",
                        "",
                        "§e» Clic pour améliorer")
                .build();
        gui.setItem(14, upgrades);
        
        // Téléportation
        ItemStack teleport = new ItemBuilder(Material.ENDER_PEARL)
                .setName("§b§l🚀 Téléportation")
                .setLore("§7Se téléporter rapidement",
                        "§7sur votre île",
                        "",
                        "§e» Clic pour se téléporter")
                .build();
        gui.setItem(16, teleport);
        
        // Reset de l'île (dangereux)
        ItemStack reset = new ItemBuilder(Material.TNT)
                .setName("§c§l💥 Reset de l'Île")
                .setLore("§c⚠️ ATTENTION ! Action irréversible !",
                        "",
                        "§7Supprime complètement votre île",
                        "§7et tous ses progrès.",
                        "",
                        "§c» Clic pour reset (confirmation requise)")
                .build();
        gui.setItem(22, reset);
        
        // Retour
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("§7← Retour")
                .setLore("§7Retourner au menu principal")
                .build();
        gui.setItem(18, backItem);
        
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
            case 4: // Informations / Renommer
                renameIsland(player);
                break;
            case 10: // Membres
                openMembersGUI(player);
                break;
            case 12: // Paramètres
                new IslandSettingsGUI(plugin).openGUI(player);
                break;
            case 14: // Améliorations
                openUpgradesGUI(player);
                break;
            case 16: // Téléportation
                teleportToIsland(player);
                break;
            case 18: // Retour
                new IslandMainGUI(plugin).openGUI(player);
                break;
            case 22: // Reset
                confirmReset(player);
                break;
        }
    }
    
    private void renameIsland(Player player) {
        player.closeInventory();
        player.sendMessage("§e✏️ Fonction de renommage bientôt disponible !");
        player.sendMessage("§7💡 Pour l'instant, utilisez les paramètres d'île.");
    }
    
    private void openMembersGUI(Player player) {
        player.closeInventory();
        player.sendMessage("§b👥 Gestion des membres bientôt disponible !");
        player.sendMessage("§7💡 Cette fonctionnalité sera ajoutée dans une future mise à jour.");
    }
    
    private void openUpgradesGUI(Player player) {
        player.closeInventory();
        player.sendMessage("§d✨ Menu d'améliorations bientôt disponible !");
        player.sendMessage("§7💡 Vous pourrez bientôt améliorer votre île avec des SkyCoins.");
    }
    
    private void teleportToIsland(Player player) {
        var island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island != null) {
            plugin.getIslandManager().teleportToIsland(player, island);
            player.sendMessage("§a✅ Téléportation vers votre île !");
            player.closeInventory();
        } else {
            player.sendMessage("§c❌ Erreur: Île non trouvée !");
        }
    }
    
    private void confirmReset(Player player) {
        player.closeInventory();
        player.sendMessage("§c⚠️ ATTENTION ! Vous êtes sur le point de supprimer votre île !");
        player.sendMessage("§7Cette action est §c§lIRRÉVERSIBLE§7 !");
        player.sendMessage("§7Tapez §f/island confirm-reset §7pour confirmer.");
        player.sendMessage("§7Ou attendez 30 secondes pour annuler.");
    }
}
package fr.skyblock.commands;

import fr.skyblock.SkyBlockPlugin;
import fr.skyblock.gui.IslandMainGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IslandCommand implements CommandExecutor, TabCompleter {
    
    private final SkyBlockPlugin plugin;
    private final IslandMainGUI mainGUI;
    
    public IslandCommand(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        this.mainGUI = new IslandMainGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Cette commande ne peut être exécutée que par un joueur !");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Vérifier les permissions
        if (!player.hasPermission("skyblock.island")) {
            player.sendMessage("§c❌ Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }
        
        // Si pas d'arguments, ouvrir le GUI principal
        if (args.length == 0) {
            mainGUI.openGUI(player);
            return true;
        }
        
        // Commandes administratives
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (player.hasPermission("skyblock.admin.reload")) {
                        plugin.getConfigManager().reloadConfigs();
                        player.sendMessage("§a✅ Configuration rechargée !");
                    } else {
                        player.sendMessage("§c❌ Permission insuffisante !");
                    }
                    break;
                    
                case "delete":
                    if (args.length < 2) {
                        player.sendMessage("§c❌ Usage: /island delete <joueur>");
                        return true;
                    }
                    if (player.hasPermission("skyblock.admin.delete")) {
                        handleDeleteCommand(player, args[1]);
                    } else {
                        player.sendMessage("§c❌ Permission insuffisante !");
                    }
                    break;
                    
                case "info":
                    if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                        var island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
                        player.sendMessage("§6=== Info de votre Île ===");
                        player.sendMessage("§7Nom: §f" + island.getName());
                        player.sendMessage("§7ID: §f" + island.getId());
                        player.sendMessage("§7Niveau: §6" + island.getLevel());
                        player.sendMessage("§7Taille: §b" + island.getSize() + "x" + island.getSize());
                        player.sendMessage("§7Biome: §a" + island.getBiome());
                        player.sendMessage("§7Membres: §e" + island.getMembers().size());
                        player.sendMessage("§7PvP: " + (island.isPvpEnabled() ? "§aActivé" : "§cDésactivé"));
                    } else {
                        player.sendMessage("§c❌ Vous n'avez pas d'île !");
                    }
                    break;
                    
                case "help":
                    showHelp(player);
                    break;
                    
                case "scoreboard":
                    plugin.getScoreboardManager().toggleScoreboard(player);
                    break;
                    
                default:
                    player.sendMessage("§c❌ Commande inconnue ! Utilisez §f/island help §cpour voir l'aide.");
                    break;
            }
        }
        
        return true;
    }
    
    private void handleDeleteCommand(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§c❌ Joueur non trouvé ou hors ligne !");
            return;
        }
        
        if (!plugin.getIslandManager().hasIsland(target.getUniqueId())) {
            player.sendMessage("§c❌ Ce joueur n'a pas d'île !");
            return;
        }
        
        var island = plugin.getIslandManager().getPlayerIsland(target.getUniqueId());
        if (plugin.getIslandManager().deleteIsland(island.getId())) {
            player.sendMessage("§a✅ Île de " + targetName + " supprimée !");
            target.sendMessage("§c❌ Votre île a été supprimée par un administrateur !");
        } else {
            player.sendMessage("§c❌ Erreur lors de la suppression de l'île !");
        }
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== Aide SkyBlock ===");
        player.sendMessage("§e/island §7- Ouvrir le menu principal");
        player.sendMessage("§e/island info §7- Informations sur votre île");
        player.sendMessage("§e/island scoreboard §7- Afficher/masquer le scoreboard");
        player.sendMessage("§e/skycoins §7- Voir vos SkyCoins");
        
        if (player.hasPermission("skyblock.admin")) {
            player.sendMessage("§c=== Commandes Admin ===");
            player.sendMessage("§c/island reload §7- Recharger la config");
            player.sendMessage("§c/island delete <joueur> §7- Supprimer une île");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("info", "help", "scoreboard");
            
            if (sender.hasPermission("skyblock.admin")) {
                commands = new ArrayList<>(commands);
                commands.addAll(Arrays.asList("reload", "delete"));
            }
            
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            // Suggérer les noms des joueurs en ligne
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
package fr.skyblock.commands;

import fr.skyblock.SkyBlockPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkyCoinsCommand implements CommandExecutor, TabCompleter {
    
    private final SkyBlockPlugin plugin;
    
    public SkyCoinsCommand(SkyBlockPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Cette commande ne peut être exécutée que par un joueur !");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Afficher ses propres SkyCoins
            int coins = plugin.getEconomyManager().getSkyCoins(player.getUniqueId());
            player.sendMessage("§6💰 Vous avez §e" + coins + " SkyCoins§6 !");
            return true;
        }
        
        // Commandes administratives
        if (!player.hasPermission("skyblock.admin")) {
            player.sendMessage("§c❌ Permission insuffisante !");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 3) {
                    player.sendMessage("§c❌ Usage: /skycoins give <joueur> <montant>");
                    return true;
                }
                handleGiveCommand(player, args[1], args[2]);
                break;
                
            case "remove":
                if (args.length < 3) {
                    player.sendMessage("§c❌ Usage: /skycoins remove <joueur> <montant>");
                    return true;
                }
                handleRemoveCommand(player, args[1], args[2]);
                break;
                
            case "set":
                if (args.length < 3) {
                    player.sendMessage("§c❌ Usage: /skycoins set <joueur> <montant>");
                    return true;
                }
                handleSetCommand(player, args[1], args[2]);
                break;
                
            case "check":
                if (args.length < 2) {
                    player.sendMessage("§c❌ Usage: /skycoins check <joueur>");
                    return true;
                }
                handleCheckCommand(player, args[1]);
                break;
                
            default:
                player.sendMessage("§c❌ Commande inconnue !");
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleGiveCommand(Player sender, String targetName, String amountStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§c❌ Joueur non trouvé ou hors ligne !");
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c❌ Montant invalide !");
            return;
        }
        
        if (amount <= 0) {
            sender.sendMessage("§c❌ Le montant doit être positif !");
            return;
        }
        
        plugin.getEconomyManager().addSkyCoins(target.getUniqueId(), amount);
        
        sender.sendMessage("§a✅ " + amount + " SkyCoins donnés à " + targetName + " !");
        target.sendMessage("§6💰 Vous avez reçu " + amount + " SkyCoins d'un administrateur !");
    }
    
    private void handleRemoveCommand(Player sender, String targetName, String amountStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§c❌ Joueur non trouvé ou hors ligne !");
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c❌ Montant invalide !");
            return;
        }
        
        if (amount <= 0) {
            sender.sendMessage("§c❌ Le montant doit être positif !");
            return;
        }
        
        boolean success = plugin.getEconomyManager().removeSkyCoins(target.getUniqueId(), amount);
        
        if (success) {
            sender.sendMessage("§a✅ " + amount + " SkyCoins retirés à " + targetName + " !");
            target.sendMessage("§c💸 " + amount + " SkyCoins vous ont été retirés par un administrateur !");
        } else {
            sender.sendMessage("§c❌ " + targetName + " n'a pas assez de SkyCoins !");
        }
    }
    
    private void handleSetCommand(Player sender, String targetName, String amountStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§c❌ Joueur non trouvé ou hors ligne !");
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c❌ Montant invalide !");
            return;
        }
        
        if (amount < 0) {
            sender.sendMessage("§c❌ Le montant ne peut pas être négatif !");
            return;
        }
        
        plugin.getEconomyManager().setSkyCoins(target.getUniqueId(), amount);
        
        sender.sendMessage("§a✅ SkyCoins de " + targetName + " définis à " + amount + " !");
        target.sendMessage("§6💰 Vos SkyCoins ont été définis à " + amount + " par un administrateur !");
    }
    
    private void handleCheckCommand(Player sender, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§c❌ Joueur non trouvé ou hors ligne !");
            return;
        }
        
        int coins = plugin.getEconomyManager().getSkyCoins(target.getUniqueId());
        sender.sendMessage("§6💰 " + targetName + " a §e" + coins + " SkyCoins§6 !");
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== Aide SkyCoins ===");
        player.sendMessage("§e/skycoins §7- Voir ses SkyCoins");
        
        if (player.hasPermission("skyblock.admin")) {
            player.sendMessage("§c=== Commandes Admin ===");
            player.sendMessage("§c/skycoins give <joueur> <montant> §7- Donner des SkyCoins");
            player.sendMessage("§c/skycoins remove <joueur> <montant> §7- Retirer des SkyCoins");
            player.sendMessage("§c/skycoins set <joueur> <montant> §7- Définir les SkyCoins");
            player.sendMessage("§c/skycoins check <joueur> §7- Voir les SkyCoins d'un joueur");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("skyblock.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("give", "remove", "set", "check");
            
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            // Suggérer les noms des joueurs en ligne
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3 && !args[0].equalsIgnoreCase("check")) {
            // Suggérer quelques montants courants
            List<String> amounts = Arrays.asList("100", "500", "1000", "5000");
            for (String amount : amounts) {
                if (amount.startsWith(args[2])) {
                    completions.add(amount);
                }
            }
        }
        
        return completions;
    }
}
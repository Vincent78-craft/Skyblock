package fr.skyblock.managers;

import fr.skyblock.SkyBlockPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorldManager {
    
    private final SkyBlockPlugin plugin;
    private World skyWorld;
    
    public WorldManager(SkyBlockPlugin plugin) {
        this.plugin = plugin;
        createSkyWorld();
    }
    
    private void createSkyWorld() {
        String worldName = "skyblock_world";
        
        // Vérifier si le monde existe déjà
        skyWorld = Bukkit.getWorld(worldName);
        
        if (skyWorld == null) {
            // Créer un nouveau monde vide
            WorldCreator creator = new WorldCreator(worldName);
            creator.type(WorldType.FLAT);
            creator.generator(new VoidChunkGenerator());
            creator.generateStructures(false);
            
            skyWorld = creator.createWorld();
            
            if (skyWorld != null) {
                // Configurer le monde
                skyWorld.setDifficulty(Difficulty.NORMAL);
                skyWorld.setSpawnFlags(true, true);
                skyWorld.setPVP(false);
                skyWorld.setAutoSave(true);
                
                // Définir les règles de jeu
                skyWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                skyWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
                skyWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
                skyWorld.setGameRule(GameRule.MOB_GRIEFING, false);
                
                plugin.getLogger().info("Monde SkyBlock créé : " + worldName);
            } else {
                plugin.getLogger().severe("Impossible de créer le monde SkyBlock !");
            }
        } else {
            plugin.getLogger().info("Monde SkyBlock existant chargé : " + worldName);
        }
    }
    
    public void generateIsland(Location center, String schematicName) {
        if (skyWorld == null) {
            plugin.getLogger().severe("Le monde SkyBlock n'est pas disponible !");
            return;
        }
        
        // Générer l'île selon le schéma choisi
        switch (schematicName.toLowerCase()) {
            case "classic":
                generateClassicIsland(center);
                break;
            case "desert":
                generateDesertIsland(center);
                break;
            case "snow":
                generateSnowIsland(center);
                break;
            default:
                generateClassicIsland(center);
                break;
        }
    }
    
    private void generateClassicIsland(Location center) {
        // Île classique avec de la terre et de l'herbe
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -3; y <= 0; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();
                    
                    if (y == 0) {
                        block.setType(Material.GRASS_BLOCK);
                    } else if (y == -1) {
                        block.setType(Material.DIRT);
                    } else {
                        block.setType(Material.STONE);
                    }
                }
            }
        }
        
        // Ajouter un arbre au centre
        Location treeLoc = center.clone().add(0, 1, 0);
        treeLoc.getBlock().setType(Material.OAK_LOG);
        treeLoc.clone().add(0, 1, 0).getBlock().setType(Material.OAK_LOG);
        
        // Feuilles
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 2; y <= 3; y++) {
                    Location leafLoc = center.clone().add(x, y, z);
                    if (leafLoc.distance(center.clone().add(0, y, 0)) <= 1.5) {
                        leafLoc.getBlock().setType(Material.OAK_LEAVES);
                    }
                }
            }
        }
        
        // Ajouter un coffre avec des objets de départ
        Location chestLoc = center.clone().add(2, 1, 0);
        chestLoc.getBlock().setType(Material.CHEST);
        
        // TODO: Remplir le coffre avec des objets de départ
    }
    
    private void generateDesertIsland(Location center) {
        // Île de désert avec du sable
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -3; y <= 0; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();
                    
                    if (y >= -1) {
                        block.setType(Material.SAND);
                    } else {
                        block.setType(Material.SANDSTONE);
                    }
                }
            }
        }
        
        // Ajouter un cactus
        Location cactusLoc = center.clone().add(1, 1, 1);
        cactusLoc.getBlock().setType(Material.CACTUS);
        cactusLoc.clone().add(0, 1, 0).getBlock().setType(Material.CACTUS);
        
        // Ajouter de la canne à sucre près de l'eau
        Location waterLoc = center.clone().add(-2, 1, 0);
        waterLoc.getBlock().setType(Material.WATER);
        waterLoc.clone().add(1, 0, 0).getBlock().setType(Material.SUGAR_CANE);
    }
    
    private void generateSnowIsland(Location center) {
        // Île de neige
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -3; y <= 0; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();
                    
                    if (y == 0) {
                        block.setType(Material.SNOW_BLOCK);
                    } else if (y == -1) {
                        block.setType(Material.DIRT);
                    } else {
                        block.setType(Material.STONE);
                    }
                }
            }
        }
        
        // Ajouter de la neige sur le dessus
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location snowLoc = center.clone().add(x, 1, z);
                snowLoc.getBlock().setType(Material.SNOW);
            }
        }
        
        // Ajouter un sapin
        Location treeLoc = center.clone().add(0, 1, 0);
        treeLoc.getBlock().setType(Material.SPRUCE_LOG);
        treeLoc.clone().add(0, 1, 0).getBlock().setType(Material.SPRUCE_LOG);
        
        // Feuilles de sapin
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location leafLoc = center.clone().add(x, 2, z);
                leafLoc.getBlock().setType(Material.SPRUCE_LEAVES);
            }
        }
    }
    
    public World getSkyWorld() {
        return skyWorld;
    }
    
    public boolean isInSkyWorld(Location location) {
        return skyWorld != null && location.getWorld().equals(skyWorld);
    }
    
    public List<String> getAvailableSchematics() {
        return Arrays.asList("classic", "desert", "snow");
    }
    
    private static class VoidChunkGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world);
        }
    }
}
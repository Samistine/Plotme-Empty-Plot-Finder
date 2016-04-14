package com.samistine.emptyplotfinder;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

//import com.worldcretornica.plotme.defaultgenerator.DefaultChunkGenerator;
//import com.worldcretornica.plotme.defaultgenerator.DefaultGenerator;
//import com.worldcretornica.plotme.defaultgenerator.DefaultPlotManager;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotMeCoreManager;
import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.api.ILocation;
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.api.IPlotMe_GeneratorManager;
import com.worldcretornica.plotme_core.bukkit.api.BukkitLocation;
import java.util.logging.Level;

public class PlotClear implements Listener {

    Main pl;
    Plugin plotMe;
    PlotMe_Core plotmeAPI;

    public PlotClear(Main instance) {
        pl = instance;
        plotMe = instance.getServer().getPluginManager().getPlugin("PlotMe");
        if (plotMe == null) {
            instance.getLogger().severe("Cannot find PlotMe!");
            return;
        }

        PlotMe_CorePlugin corePlug = (PlotMe_CorePlugin) plotMe;
        plotmeAPI = corePlug.getAPI();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // if (!plotClearPattern.matcher(event.getMessage()).find()) {
        // return;
        // }
        // String randpass = Main.getRandomPassword();
        // String cmdsam = event.getMessage().toLowerCase();
        String msg = event.getMessage();
        Player player = event.getPlayer();
        IPlayer iPlayer = plotmeAPI.getServerBridge().getPlayer(
                event.getPlayer().getUniqueId());

        if (msg.equalsIgnoreCase("/p clear") || msg.equalsIgnoreCase("/plot clear") || msg.equalsIgnoreCase("/plotme clear")) {
            event.setCancelled(true);
            warnPlayer(player);
            return;
        }
        if (msg.equalsIgnoreCase("/p clear confirm yes") || msg.equalsIgnoreCase("/plot clear confirm yes") || msg.equalsIgnoreCase("/plotme clear confirm yes")) {
            event.setCancelled(true);
            if (!player.hasPermission("PlotMeFix.clear")) { // changed
                player.sendMessage(ChatColor.RED + plotmeAPI.getUtil().C("MsgPermissionDenied"));
                return;
            }

            PlotMeCoreManager manager = PlotMeCoreManager.getInstance();

            // if (!PlotManager.isPlotWorld(player)) {
            if (!manager.isPlotWorld(iPlayer)) {
                player.sendMessage(ChatColor.RED + plotmeAPI.getUtil().C("MsgNotPlotWorld"));
                return;
            }

            String plotId = manager.getPlotId(iPlayer.getLocation());

            if (plotId == null) {
                player.sendMessage(ChatColor.RED + plotmeAPI.getUtil().C("MsgNoPlotFound"));
                return;
            }

            if (manager.isPlotAvailable(plotId, iPlayer)) {
                player.sendMessage(plotmeAPI.getUtil().C("MsgThisPlot") + plotId + plotmeAPI.getUtil().C("MsgHasNoOwner"));
                return;
            }

            Plot plot = manager.getPlotById(plotId, iPlayer);

            if (plot.isProtect()) {
                player.sendMessage(ChatColor.RED + plotmeAPI.getUtil().C("MsgPlotProtectedCannotClear"));
                return;
            }

            if (!plot.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("PlotMe.admin.clear")) {
                player.sendMessage(plotmeAPI.getUtil().C("MsgThisPlot") + plotId + plotmeAPI.getUtil().C("MsgNotYoursNotAllowedClear"));
                return;
            }

            final UUID playerId = player.getUniqueId();
            final World world = player.getWorld();

            ILocation iTop = manager.getPlotTopLoc(iPlayer.getWorld(), plotId);
            Location top = ((BukkitLocation) iTop).getLocation();
            ILocation iBottom = manager.getPlotBottomLoc(iPlayer.getWorld(), plotId);
            Location bottom = ((BukkitLocation) iBottom).getLocation();
            final IPlotMe_GeneratorManager plotMapInfo = plotmeAPI.getGenManager(plot.getWorld().toLowerCase());

            //findFile(System.getProperty("user.dir"), plot.getWorld(), "PlotFloorBlock");
            //findFile(System.getProperty("user.dir"), plot.getWorld(), "FillBlock");
            String floorBlock = pl.getYamlFile().getString("worlds." + world.getName() + ".PlotFloorBlock");
            String fillBlock = pl.getYamlFile().getString("worlds." + world.getName().toLowerCase() + ".FillBlock");

            pl.getLogger().log(Level.INFO, "{0}({1}) is clearing plot", new Object[]{player.getName(), player.getUniqueId()});
            pl.getLogger().info(floorBlock);
            pl.getLogger().info(fillBlock);

            String[] splitFloor = floorBlock.split(":");
            final int PlotFloorBlockId = Integer.valueOf(splitFloor[0]);

            String[] splitBlock = fillBlock.split(":");
            final int PlotFillingBlockId = Integer.valueOf(splitBlock[0]);

            final int PlotFloorBlockValue;
            final int PlotFillingBlockValue;

            if (splitFloor.length > 1) {
                PlotFloorBlockValue = Integer.valueOf(splitFloor[1]);
            } else {
                PlotFloorBlockValue = -1;
            }
            if (splitBlock.length > 1) {
                PlotFillingBlockValue = Integer.valueOf(splitBlock[1]);
            } else {
                PlotFillingBlockValue = -1;
            }

            final int bottomX = bottom.getBlockX();
            final int topX = top.getBlockX();
            final int bottomZ = bottom.getBlockZ();
            final int topZ = top.getBlockZ();

            int minChunkX = (int) Math.floor((double) bottomX / 16);
            int maxChunkX = (int) Math.floor((double) topX / 16);
            int minChunkZ = (int) Math.floor((double) bottomZ / 16);
            int maxChunkZ = (int) Math.floor((double) topZ / 16);

            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    Chunk chunk = world.getChunkAt(cx, cz);

                    for (Entity entity : chunk.getEntities()) {
                        Location entityLocation = entity.getLocation();

                        if (!(entity instanceof Player)
                                && entityLocation.getBlockX() >= bottom.getBlockX()
                                && entityLocation.getBlockX() <= top.getBlockX()
                                && entityLocation.getBlockZ() >= bottom.getBlockZ()
                                && entityLocation.getBlockZ() <= top.getBlockZ()) {

                            entity.remove();
                        }
                    }
                }
            }

            //Regenerate full chunks using api method, this is extremely fast compared to other methods
            ChunkUtils utils = new ChunkUtils();
            ChunkUtils.FoundChunks fc = utils.findChunksInArea(world, bottomX, topX, bottomZ, topZ);
            for (Chunk chunk : fc.completeChunks) {
                world.regenerateChunk(chunk.getX(), chunk.getZ());
            }

            //Store locations that need resetting
            final Queue<Location> blocks = new ConcurrentLinkedQueue<>();

            //Begin finding blocks that need reseting
            final BukkitTask blockFindTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (int x = bottomX; x <= topX; x++) {
                        for (int z = bottomZ; z <= topZ; z++) {
                            for (int y = 1; y < world.getMaxHeight(); ++y) {
                                Location location = new Location(world, x, y, z);

                                if (location.getBlockY() < plotMapInfo.getRoadHeight(world.getName())) {
                                    if (location.getBlock().getTypeId() != PlotFillingBlockId
                                            || (PlotFillingBlockValue != -1 && location.getBlock().getData() != (byte) PlotFillingBlockValue)) {

                                        blocks.add(location);
                                    }
                                } else if (location.getBlockY() == plotMapInfo.getRoadHeight(world.getName())) {
                                    if (location.getBlock().getTypeId() != PlotFloorBlockId
                                            || (PlotFloorBlockValue != -1 && location.getBlock().getData() != (byte) PlotFloorBlockValue)) {

                                        blocks.add(location);
                                    }
                                } else if (location.getBlock().getType() != Material.AIR) {
                                    blocks.add(location);
                                }
                            }
                        }
                    }
                }
            }.runTaskAsynchronously(pl);//Probably not safe but ehh it works

            //Begin resetting blocks that need it
            new BukkitRunnable() {
                @Override
                public void run() {
                    // int blocksPerTick =
                    // getConfig().getInt("clear-queue.blocks-per-tick");
                    int blocksPerTick = 50;
                    Location location;

                    for (int i = 0; i < blocksPerTick && (location = blocks.poll()) != null; ++i) {
                        if (!location.getChunk().isLoaded()) {
                            location.getChunk().load(true);
                        }

                        if (location.getBlockY() < plotMapInfo.getRoadHeight(world.getName())) {
                            location.getBlock().setTypeIdAndData(PlotFillingBlockId, (byte) PlotFillingBlockValue, false);

                        } else if (location.getBlockY() == plotMapInfo.getRoadHeight(world.getName())) {
                            location.getBlock().setTypeIdAndData(PlotFloorBlockId, (byte) PlotFloorBlockValue, false);

                        } else if (location.getBlock().getType() != Material.AIR) {
                            location.getBlock().setTypeIdAndData(0, (byte) 0, false);

                        }
                    }

                    if (blocks.isEmpty() && !pl.getServer().getScheduler().isCurrentlyRunning(blockFindTask.getTaskId()) && !pl.getServer().getScheduler().isQueued(blockFindTask.getTaskId())) {

                        cancel();

                        Player player = Bukkit.getPlayer(playerId);

                        if (player != null) {
                            player.sendMessage(plotmeAPI.getUtil().C("MsgPlotCleared"));
                        }
                    }
                }
            }.runTaskTimer(pl, 1, 1); // changed
        }
        if (!msg.equalsIgnoreCase("/p clear confirm yes")
                && (msg.toLowerCase().startsWith("/p clear")
                || msg.toLowerCase().startsWith("/plot clear") || msg
                .toLowerCase().startsWith("/plotme clear"))) {
            event.setCancelled(true);
            warnPlayer(player);
        }
    }

    public void warnPlayer(Player player) {
        String msgplayertoconfirm = ChatColor.translateAlternateColorCodes('&', "&4&lARE YOU SURE YOU WANT TO PERMENANTLY DELETE ALL DATA FROM THIS PLOT?");
        String msgplayertoconfirm2 = ChatColor.translateAlternateColorCodes('&', "&4&lALL DATA FROM THIS PLOT WILL BE LOST");
        String msgplayertoconfirm3 = ChatColor.translateAlternateColorCodes('&', "&4&lIF YOU ARE COMPLETLY SURE DO &2&l/p clear confirm yes");
        player.sendMessage(msgplayertoconfirm);
        player.sendMessage(msgplayertoconfirm2);
        player.sendMessage(msgplayertoconfirm3);
    }

    /*public void findFile(String folder, String world, String element) {
     for (File fileEntry : new File(folder).listFiles()) {
     if (fileEntry.isDirectory()) {
     findFile(fileEntry.getAbsolutePath(), world, element);
     } else {
     if (fileEntry.getName().contains("config") && fileEntry.getAbsolutePath().contains("PlotMe-DefaultGenerator")) {
     try {
     Map<String,Object> orig = (Map<String,Object>)(new Yaml()).load(
     new FileReader(fileEntry));
     Map<String,Object> worlds = (Map<String,Object>)orig.get("worlds");
     Map<String,Object> elements = (Map<String,Object>)worlds.get(world);
     pl.getLogger().info("ert " + (String) elements.get(element));
     if(element.equals("PlotFloorBlock"))
     ground = (String) elements.get(element);
     else if(element.equals("FillBlock"))
     fill = (String) elements.get(element);
     return;
     } catch (FileNotFoundException e) {
     e.printStackTrace();
     }
     }
     }
     }
     }*/
}

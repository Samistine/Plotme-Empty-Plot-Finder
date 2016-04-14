/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.samistine.emptyplotfinder;

import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotMeCoreManager;
import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.api.ILocation;
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.api.IPlotMe_GeneratorManager;
import com.worldcretornica.plotme_core.api.IWorld;
import com.worldcretornica.plotme_core.bukkit.api.BukkitLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author Samuel
 */
public class PlotUtils {

    Main pl;
    PlotMe_Core plotmeAPI;

    public PlotUtils(Main pl, PlotMe_Core plotmeAPI) {
        this.pl = pl;
        this.plotmeAPI = plotmeAPI;
    }

    public boolean isEmpty(Plot plot) {
        // if (!plotClearPattern.matcher(event.getMessage()).find()) {
        // return;
        // }
        // String randpass = Main.getRandomPassword();
        // String cmdsam = event.getMessage().toLowerCase();

        PlotMeCoreManager manager = PlotMeCoreManager.getInstance();

        final String plotId = plot.getId();
        final IWorld world = plotmeAPI.getServerBridge().getWorld(plot.getWorld());

        ILocation iTop = manager.getPlotTopLoc(world, plotId);
        Location top = ((BukkitLocation) iTop).getLocation();
        ILocation iBottom = manager.getPlotBottomLoc(world, plotId);
        Location bottom = ((BukkitLocation) iBottom).getLocation();
        final IPlotMe_GeneratorManager plotMapInfo = plotmeAPI.getGenManager(plot.getWorld().toLowerCase());

        //findFile(System.getProperty("user.dir"), plot.getWorld(), "PlotFloorBlock");
        //findFile(System.getProperty("user.dir"), plot.getWorld(), "FillBlock");
        String floorBlock = pl.getYamlFile().getString("worlds." + world.getName() + ".PlotFloorBlock");
        String fillBlock = pl.getYamlFile().getString("worlds." + world.getName().toLowerCase() + ".FillBlock");

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

        //Store locations that need resetting
        boolean hasBlocks = false;

        final World world2 = Bukkit.getWorld(world.getName());

        //Begin finding blocks that need reseting
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                for (int y = 1; y < world2.getMaxHeight(); ++y) {
                    Location location = new Location(world2, x, y, z);

                    if (location.getBlockY() < plotMapInfo.getRoadHeight(world.getName())) {
                        if (location.getBlock().getTypeId() != PlotFillingBlockId
                                || (PlotFillingBlockValue != -1 && location.getBlock().getData() != (byte) PlotFillingBlockValue)) {

                            hasBlocks = true;
                            break;
                        }
                    } else if (location.getBlockY() == plotMapInfo.getRoadHeight(world.getName())) {
                        if (location.getBlock().getTypeId() != PlotFloorBlockId
                                || (PlotFloorBlockValue != -1 && location.getBlock().getData() != (byte) PlotFloorBlockValue)) {

                            hasBlocks = true;
                            break;
                        }
                    } else if (location.getBlock().getType() != Material.AIR) {
                        hasBlocks = true;
                        break;
                    }
                }
            }
        }
        return !hasBlocks;
    }
}

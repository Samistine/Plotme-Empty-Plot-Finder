package com.samistine.emptyplotfinder;

import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotMeCoreManager;
import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Main extends JavaPlugin implements Listener {
    //static String randpass = PassGenerator.main(null);

    private PlotMe_Core plotmeAPI;
    private YamlConfiguration yamlFile;

    @Override
    public void onEnable() {
        Plugin plotMe = getServer().getPluginManager().getPlugin("PlotMe");
        if (plotMe == null) {
            getLogger().severe("Cannot find PlotMe!");
            return;
        }
        PlotMe_CorePlugin plotme = (PlotMe_CorePlugin) plotMe;
        plotmeAPI = plotme.getAPI();

        File f = new File(getDataFolder().getParent() + "/PlotMe/PlotMe-DefaultGenerator/config.yml");
        yamlFile = YamlConfiguration.loadConfiguration(f);

        getServer().getPluginManager().registerEvents(this, this);
    }

    public PlotMe_Core getplotmeAPI() {
        return plotmeAPI;
    }

    public YamlConfiguration getYamlFile() {
        return yamlFile;
    }

    @EventHandler
    private void onPre(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/test22")) {
            PlotUtils utils = new PlotUtils(this, plotmeAPI);

            PlotMeCoreManager manager = PlotMeCoreManager.getInstance();
            IPlayer iPlayer = plotmeAPI.getServerBridge().getPlayer(event.getPlayer().getUniqueId());
            String plotId = manager.getPlotId(iPlayer.getLocation());
            Plot plot = manager.getPlotById(plotId, iPlayer);

            event.getPlayer().sendMessage("isEmpty() = " + utils.isEmpty(plot));
        }
    }
}

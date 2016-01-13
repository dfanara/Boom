package com.shdwlf.boom;

import com.shdwlf.boom.listeners.BlockListener;
import com.shdwlf.boom.listeners.InteractListener;
import com.shdwlf.boom.util.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class Boom extends JavaPlugin {

    private BlockManager blockManager;
    private Metrics.Graph graph;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        blockManager = new BlockManager(this);
        blockManager.load();

        registerListeners();

        if(getConfig().getLong("autosave") > 0)
            getServer().getScheduler().runTaskTimer(this, () -> {
                System.out.println("[Boom] Autosaving configuration.");
            }, getConfig().getLong("autosave", 6000), getConfig().getLong("autosave", 6000));

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
            Bukkit.getLogger().info("[Boom] Metrics enabled.");
        } catch (IOException e) {
            Bukkit.getLogger().info("[Boom] Failed to enable Metrics. " + e.getLocalizedMessage());
        }
    }

    private void updateGraph(Metrics.Graph licenseGraph) {
        licenseGraph.addPlotter(new Metrics.Plotter() {
            @Override
            public int getValue() {
                int count = blockManager.getDetonationCount();
                blockManager.resetDetonationCount();
                return count;
            }
        });
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
    }

    @Override
    public void onDisable() {
        blockManager.save();
        Bukkit.getScheduler().cancelAllTasks();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "Boom" + ChatColor.GRAY + "] " + ChatColor.RESET + "Boom version " + ChatColor.RED
                + getDescription().getVersion() + ChatColor.RESET + " by " + ChatColor.RED + "Shadowwolf97" + ChatColor.RESET + ".");
        return true;
    }

    public BlockManager getBlockManager() {
        return this.blockManager;
    }
}
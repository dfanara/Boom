package com.shdwlf.boom;

import com.shdwlf.boom.listeners.BlockListener;
import com.shdwlf.boom.listeners.InteractListener;
import com.shdwlf.boom.util.Metrics;
import com.shdwlf.boom.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Boom extends JavaPlugin {

    private BlockManager blockManager;
    public static boolean SNAPSHOT;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        SNAPSHOT = getDescription().getVersion().contains("SNAPSHOT");

        blockManager = new BlockManager(this);
        blockManager.load();


        registerListeners();

        if (getConfig().getLong("autosave") > 0) {
            getServer().getScheduler().runTaskTimer(this, () -> {
                System.out.println("[Boom] Autosaving configuration.");
                blockManager.save();
            }, getConfig().getLong("autosave", 6000), getConfig().getLong("autosave", 6000));
        }

        if(!SNAPSHOT) {
            if(getConfig().getBoolean("update-checks", true)) {
                UpdateChecker updateChecker = new UpdateChecker(this, "https://gist.githubusercontent.com/dfanara/7952b9bb2a76302ef482/raw/Boom.yml");
                updateChecker.updateCycle(true);
            }

            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
                Bukkit.getLogger().info("[Boom] Metrics enabled.");
            } catch (IOException e) {
                Bukkit.getLogger().info("[Boom] Failed to enable Metrics. " + e.getLocalizedMessage());
            }
        }

        if(SNAPSHOT) {
            getLogger().info("Running a SNAPSHOT version. No support will be provided for this version, please use an official release.");
        }
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
package com.shdwlf.boom;

import com.shdwlf.KeenMetrics;
import com.shdwlf.UpdateChecker;
import com.shdwlf.boom.listeners.BlockListener;
import com.shdwlf.boom.listeners.InteractListener;
import com.shdwlf.boom.util.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Boom extends JavaPlugin {

    public static boolean SNAPSHOT;
    public Economy economy = null;
    private BlockManager blockManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        SNAPSHOT = getDescription().getVersion().contains("SNAPSHOT");

        blockManager = new BlockManager(this);
        blockManager.load();

        registerListeners();

        if (getConfig().getLong("autosave") > 0) {
            getServer().getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    System.out.println("[Boom] Autosaving configuration.");
                    blockManager.save();
                }
            }, getConfig().getLong("autosave", 6000), getConfig().getLong("autosave", 6000));
        }

        setupEconomy();
        if (economy != null) {
            System.out.println("[Boom] Hooked into Vault!");
        } else {
            System.out.println("[Boom] Vault not found. Economy features disabled.");
        }

        if (SNAPSHOT) {
            getLogger().info("Running a SNAPSHOT version. No support will be provided for this version, please use an official release.");
        } else {
            if (getConfig().getBoolean("update-checks", true)) {
                UpdateChecker updateChecker = new UpdateChecker(this, "https://gist.githubusercontent.com/dfanara/7952b9bb2a76302ef482/raw/Boom.yml");
                updateChecker.updateCycle(false);
            }

            try {
                setupMetrics();
                Bukkit.getLogger().info("[Boom] Metrics enabled.");
            } catch (IOException e) {
                Bukkit.getLogger().info("[Boom] Failed to enable Metrics. " + e.getLocalizedMessage());
            }
        }
    }

    private void setupMetrics() throws IOException {
        Metrics metrics = new Metrics(this);
        metrics.start();

        KeenMetrics keenMetrics = new KeenMetrics(this);
        keenMetrics.registerMetricReporter(new KeenMetrics.KeenMetricReporter() {
            @Override
            public HashMap<String, Object> getData() {
                HashMap<String, Object> data = new HashMap<String, Object>();
                data.put("registered", blockManager.getRegisterCount());
                data.put("detonated", blockManager.getDetonationCount());
                return data;
            }
        });

        keenMetrics.start();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
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
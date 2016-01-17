package com.shdwlf.boom.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class UpdateChecker implements Listener {

    private final String path;
    private final String currentVersion;
    private final JavaPlugin plugin;
    private final YamlConfiguration versionData;
    private List<String> versionList;
    private boolean loaded = false;

    /**
     * @param path Path to plaintext version file. (ex. https://raw.githubusercontent.com/dfanara/Boom/master/VERSION)
     */
    public UpdateChecker(JavaPlugin plugin, String path) {
        this.path = path;
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.versionData = new YamlConfiguration();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private static boolean saveFile(String url, String file) {
        System.out.println("Downloading Update From: " + url);
        try {
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            if (!Bukkit.getUpdateFolderFile().exists())
                Bukkit.getUpdateFolderFile().mkdir();
            FileOutputStream fos = new FileOutputStream(new File(Bukkit.getUpdateFolderFile(), file));
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return true;
        } catch (Exception e) {
            System.out.println("Could Not Download Update: " + e.getLocalizedMessage());
        }
        return false;
    }

    private static Integer versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        } else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

    /**
     * Default method for handling update checking.
     */
    public void updateCycle(boolean allowDownload) {
        print("Checking for updates...");
        fetchVersionData();

        if (isUpdateAvailable()) {
            print("An update has been found (v" + getNewestVersion() + ")");
            if (hasMessage()) {
                print(ChatColor.RED + "A message has been provided with this update: ");
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage()));
            }

            if (hasDownloadLink() && allowDownload) {
                print(ChatColor.RED + "Automatically downloading update...");
                downloadUpdate();
                print(ChatColor.RED + "Update downloaded. Please restart your server.");
            } else if (isUpdateUrgent()) {
                print(ChatColor.RED + "This update requires IMMEDIATE attention. Please update now.");
            }
        } else {
            print("Running the latest version!");
        }
    }

    private void print(String s) {
        Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] " + s);
    }

    public void fetchVersionData() {
        try {
            URL url = new URL(path);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            versionData.load(in);

            versionList = new ArrayList<>();
            ConfigurationSection updates = versionData.getConfigurationSection("updates");
            for (String s : updates.getKeys(false)) {
                versionList.add(s.replace("_", "."));
            }

            loaded = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getNewestVersion() {
        return getNewestVersion(false);
    }

    public String getNewestVersion(boolean forYml) {
        if (!loaded)
            fetchVersionData();
        if (forYml)
            return versionList.get(0).replace(".", "_");
        return versionList.get(0);
    }

    public boolean isUpdateAvailable() {
        if (!loaded)
            fetchVersionData();

        return versionCompare(getNewestVersion(), currentVersion) > 0;
    }

    public boolean isUpdateUrgent() {
        if (!loaded)
            fetchVersionData();

        return versionData.getBoolean("updates." + getNewestVersion(true) + ".urgent", false);
    }

    public boolean hasDownloadLink() {
        if (!loaded)
            fetchVersionData();

        return versionData.contains("update-url") && !versionData.getString("update-url").equals("");
    }

    public String getDownloadLink() {
        if (!loaded)
            fetchVersionData();

        if (hasDownloadLink())
            return versionData.getString("update-url");
        return "";
    }

    public void downloadUpdate() {
        if (!loaded)
            fetchVersionData();

        if (hasDownloadLink()) {
            saveFile(getDownloadLink(), plugin.getName() + ".jar");
        }
    }

    public boolean hasMessage() {
        return versionData.contains("updates." + getNewestVersion(true) + ".message");
    }

    public String getMessage() {
        return versionData.getString("updates." + getNewestVersion(true) + ".message");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOp()) {
                    event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "Updater" + ChatColor.GRAY + "] " + ChatColor.RED + "Update Found For " + ChatColor.BOLD + plugin.getName());
                    if (hasMessage()) {
                        event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "Updater" + ChatColor.GRAY + "] " + ChatColor.translateAlternateColorCodes('&', getMessage()));
                    } else if (isUpdateUrgent()) {
                        event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "Updater" + ChatColor.GRAY + "] " + ChatColor.RED + "An Urgent Update Has Been Found For " + ChatColor.BOLD + plugin.getName());
                    }

                    if (hasDownloadLink())
                        event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "Updater" + ChatColor.GRAY + "] " + ChatColor.RED + "Please restart your server to complete the update.");
                    else
                        event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "Updater" + ChatColor.GRAY + "] " + ChatColor.RED + "Please update immediately to prevent unexpected behavior.");
                }
            }
        }, 5L);
    }
}

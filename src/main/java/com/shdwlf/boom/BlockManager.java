package com.shdwlf.boom;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;

import java.util.ArrayList;
import java.util.List;

public class BlockManager {

    private final Boom plugin;
    //Block format: world,x,y,z
    private List<String> registeredBlocks = new ArrayList<>();
    private int detonationCount = 0;
    private int registerCount = 0;

    public BlockManager(Boom plugin) {
        this.plugin = plugin;
    }

    public static String formatString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public void load() {
        registeredBlocks = plugin.getConfig().getStringList("registered-blocks");
    }

    public void save() {
        plugin.getConfig().set("registered-blocks", registeredBlocks);
        plugin.saveConfig();
    }

    public void registerBlock(Block block) {
        registerBlock(block.getLocation());
    }

    public void registerBlock(Location location) {
        registerCount++;
        registeredBlocks.add(formatString(location));
    }

    public boolean isRegistered(Block block) {
        return isRegistered(block.getLocation());
    }

    public boolean isRegistered(Location location) {
        return registeredBlocks.contains(formatString(location));
    }

    public void unregisterBlock(Block block) {
        unregisterBlock(block.getLocation());
    }

    public void unregisterBlock(Location location) {
        registeredBlocks.remove(formatString(location));
    }

    public void detonateBlock(Block block) {
        detonateBlock(block, 80);
    }

    public void detonateBlock(Block block, int fuseTicks) {
        plugin.getBlockManager().unregisterBlock(block);
        block.breakNaturally();
        TNTPrimed tnt = (TNTPrimed) block.getLocation().getWorld().spawnEntity(block.getLocation().clone().add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
        tnt.setFuseTicks(fuseTicks);
        detonationCount++;
    }

    public int getDetonationCount() {
        int count = detonationCount;
        detonationCount = 0;
        return count;
    }

    public int getRegisterCount() {
        int count = registerCount;
        registerCount = 0;
        return count;
    }
}

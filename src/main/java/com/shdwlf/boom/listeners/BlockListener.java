package com.shdwlf.boom.listeners;

import com.shdwlf.boom.Boom;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

    private final Boom plugin;

    public BlockListener(Boom plugin) {
        this.plugin = plugin;
    }

    /**
     * Listen for blocks triggered by TNT
     */
    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        if (event.getOldCurrent() == 0 && event.getNewCurrent() > 0) {
            Block triggeredBlock = event.getBlock();
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN}) {
                Block activatedBlock = triggeredBlock.getRelative(face);
                if (plugin.getBlockManager().isRegistered(activatedBlock)) {
                    plugin.getBlockManager().detonateBlock(activatedBlock);
                }
            }
        }
    }

    /**
     * Listen for block broken by pickaxe / sheers
     */
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getBlockManager().isRegistered(event.getBlock())) {
            if ((event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.SHEARS && event.getPlayer().hasPermission("boom.bypass.shears"))
                    || (plugin.getConfig().getBoolean("ignore-creative", false) && event.getPlayer().getGameMode() == GameMode.CREATIVE)
                    || event.getPlayer().hasPermission("boom.bypass")) {
                //Give block + tnt
                event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.TNT, 1));
                event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType(), 1));
                plugin.getBlockManager().unregisterBlock(event.getBlock());
            } else {
                plugin.getBlockManager().detonateBlock(event.getBlock());
            }
        }
    }

    /**
     * Listen for blocks that were involved in an explosion
     */
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().stream().filter(block -> plugin.getBlockManager().isRegistered(block))
                .forEach(block -> plugin.getBlockManager().detonateBlock(block, (int) (Math.random() * 15) + 10));
    }

}

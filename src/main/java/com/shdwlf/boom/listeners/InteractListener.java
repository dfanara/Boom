package com.shdwlf.boom.listeners;

import com.shdwlf.boom.Boom;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    private final Boom plugin;

    public InteractListener(Boom plugin) {
        this.plugin = plugin;
    }

    /**
     * Listen for player to shift click on TNT with a block
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
            Block clickedBlock = event.getClickedBlock();
            if(clickedBlock != null && clickedBlock.getType() == Material.TNT) {
                ItemStack inHand = event.getPlayer().getItemInHand();
                if(inHand != null && inHand.getType().isBlock() && inHand.getType() != Material.TNT && inHand.getType().isOccluding()) {
                    clickedBlock.setType(inHand.getType());
                    clickedBlock.setData((byte) inHand.getDurability());
                    clickedBlock.getState().update();
                    //TODO: remove block from player inventory
                    plugin.getBlockManager().registerBlock(clickedBlock);
                    event.getPlayer().getLocation().getWorld().playSound(event.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                    event.setCancelled(true);
                }
            }
        }
    }
}

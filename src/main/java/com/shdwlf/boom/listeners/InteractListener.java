package com.shdwlf.boom.listeners;

import com.shdwlf.boom.Boom;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.TNT) {
                event.setCancelled(true);
                ItemStack inHand = event.getPlayer().getItemInHand();
                if (!event.getPlayer().hasPermission("boom.use")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission.");
                    return;
                }
                if (inHand != null && inHand.getType().isBlock() && inHand.getType() != Material.TNT && inHand.getType().isOccluding() && checkEconomy(event.getPlayer())) {
                    clickedBlock.setType(inHand.getType());
                    clickedBlock.setData((byte) inHand.getDurability());
                    clickedBlock.getState().update();
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        //Remove item from player
                        if (inHand.getAmount() > 1) {
                            inHand.setAmount(inHand.getAmount() - 1);
                            event.getPlayer().setItemInHand(inHand);
                            event.getPlayer().updateInventory();
                        } else {
                            event.getPlayer().setItemInHand(null);
                            event.getPlayer().updateInventory();
                        }
                    }

                    plugin.getBlockManager().registerBlock(clickedBlock);

                    if (plugin.economy != null)
                        plugin.economy.withdrawPlayer(event.getPlayer(), plugin.getConfig().getDouble("economy.create-cost", 1000));

                    event.getPlayer().getLocation().getWorld().playSound(event.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                }
            }
        }
    }

    private boolean checkEconomy(Player player) {
        if (plugin.economy == null)
            return true;
        else {
            double cost = plugin.getConfig().getDouble("economy.create-cost", 1000);
            if (plugin.economy.has(player, cost)) {
                return true;
            } else {
                String formatted = plugin.economy.format(cost);
                String message = plugin.getConfig().getString("lang.not-enough-money", "You do not have enough money &8(&c%cost%&8)&c.").replace("%cost", formatted);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang.prefix", "&7[&cBoom&7] &r")) + ChatColor.translateAlternateColorCodes('&', message));
                return false;
            }
        }
    }

}

package org.kitteh.vanish.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.kitteh.vanish.VanishPlugin;

public final class Listen119 implements Listener {
    private final VanishPlugin plugin;

    public Listen119(VanishPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
        Entity trigger = event.getEntity();
        // sculk sensors, calibrated sculk sensors, shriekers
        if (trigger instanceof Player player && this.plugin.getManager().isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGenericGameEvent(GenericGameEvent event) {
        // vibrations for every listener: sculk, wardens, allays
        if (event.getEntity() instanceof Player player && this.plugin.getManager().isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Material blockType;
        if (event.getAction() == Action.PHYSICAL && ((blockType = event.getClickedBlock().getType()) == Material.SCULK_SENSOR || blockType == Material.SCULK_SHRIEKER)) {
            if (this.plugin.getManager().isVanished(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }
}
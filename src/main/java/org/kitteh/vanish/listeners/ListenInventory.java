package org.kitteh.vanish.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.kitteh.vanish.VanishPlugin;

public final class ListenInventory implements Listener {
    private final VanishPlugin plugin;

    public ListenInventory(VanishPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (this.plugin.chestFakeInUse(event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (this.plugin.chestFakeInUse(event.getPlayer())) {
            this.plugin.chestFakeClose(event.getPlayer());
        }
    }
}
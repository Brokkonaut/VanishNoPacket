package org.kitteh.vanish.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.LazyMetadataValue.CacheStrategy;
import org.kitteh.vanish.VanishCheck;
import org.kitteh.vanish.VanishPerms;
import org.kitteh.vanish.VanishPlugin;

public final class ListenPlayerJoin implements Listener {
    private final VanishPlugin plugin;

    public ListenPlayerJoin(VanishPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinEarly(PlayerJoinEvent event) {
        event.getPlayer().setMetadata("vanished", new LazyMetadataValue(this.plugin, CacheStrategy.NEVER_CACHE, new VanishCheck(this.plugin.getManager(), event.getPlayer().getName())));
        this.plugin.getManager().resetSeeing(event.getPlayer());
        if (VanishPerms.joinVanished(event.getPlayer())) {
            this.plugin.getManager().toggleVanishQuiet(event.getPlayer(), false);
            this.plugin.hooksVanish(event.getPlayer());
        }
        this.plugin.hooksJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinLate(PlayerJoinEvent event) {
        if (VanishPerms.joinWithoutAnnounce(event.getPlayer())) {
            this.plugin.getManager().getAnnounceManipulator().addToDelayedAnnounce(event.getPlayer().getName());
            event.setJoinMessage(null);
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            final StringBuilder statusUpdate = new StringBuilder();
            if (this.plugin.getManager().isVanished(event.getPlayer())) {
                String message = ChatColor.DARK_AQUA + "You have joined vanished.";
                if (VanishPerms.canVanish(event.getPlayer())) {
                    message += " To appear: /vanish";
                }
                event.getPlayer().sendMessage(message);
                statusUpdate.append("vanished");
            }
            if (VanishPerms.joinWithoutAnnounce(event.getPlayer())) {
                if (statusUpdate.length() != 0) {
                    statusUpdate.append(" and ");
                }
                statusUpdate.append("silently");
            }
            if (statusUpdate.length() != 0) {
                this.plugin.messageStatusUpdate(ChatColor.DARK_AQUA + event.getPlayer().getName() + " has joined " + statusUpdate.toString());
            }
        });
    }
}
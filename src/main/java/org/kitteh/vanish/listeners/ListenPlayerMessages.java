package org.kitteh.vanish.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.kitteh.vanish.Settings;
import org.kitteh.vanish.VanishPerms;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.utils.Messages;

public final class ListenPlayerMessages implements Listener {
    private final VanishPlugin plugin;

    public ListenPlayerMessages(VanishPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        if (this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canNotChat(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().toLowerCase().startsWith("/me ") && this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canNotChat(event.getPlayer())) {
            event.setCancelled(true);
        }
        if (Settings.getEnablePermTest()) {
            final String[] split = event.getMessage().split(" ");
            if ((split.length > 1) && split[0].equalsIgnoreCase("/permtest")) {
                final boolean selfTest = VanishPerms.permTestSelf(event.getPlayer());
                final boolean otherTest = VanishPerms.permTestOther(event.getPlayer());
                if (!selfTest && !otherTest) {
                    return;
                }
                final Component message;
                String permission;
                if ((split.length == 2) && selfTest) {
                    permission = split[1];
                    message = Component.text(event.getPlayer().hasPermission(permission) ? "You have " : "You do not have ");
                } else if ((split.length == 3) && otherTest) {
                    final Player target = this.plugin.getServer().getPlayerExact(split[1]);
                    if (target == null) {
                        event.getPlayer().sendMessage(Messages.prefixed(Component.text("Cannot find player: "), Component.text(split[1], NamedTextColor.AQUA)));
                        event.setCancelled(true);
                        return;
                    }
                    permission = split[2];
                    message = Component.text().append(Component.text("Player "), Component.text(target.getName(), NamedTextColor.AQUA), Component.text(target.hasPermission(permission) ? " has " : " does not have ")).build();
                } else {
                    return;
                }
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.prefixed(message, Component.text(permission, NamedTextColor.AQUA)));
            }

        }
    }
}
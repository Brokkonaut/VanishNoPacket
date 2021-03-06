package org.kitteh.vanish.listeners;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.kitteh.vanish.VanishManager;

public final class ListenServerPing implements Listener {
    private final VanishManager manager;

    public ListenServerPing(VanishManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void ping(ServerListPingEvent event) {
        try {
            final Set<String> invisibles = this.manager.getVanishedPlayers();
            final Iterator<Player> players = event.iterator();
            while (players.hasNext()) {
                Player player = players.next();
                if (invisibles.contains(player.getName())) {
                    players.remove();
                }
            }
        } catch (UnsupportedOperationException ex) {
            // ignored, some implementations throw this exception on event.iterator()
        }
    }
}

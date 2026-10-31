package org.kitteh.vanish;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.kitteh.vanish.hooks.HookManager.HookType;
import org.kitteh.vanish.hooks.plugins.VaultHook;
import org.kitteh.vanish.utils.Messages;

/**
 * Controller of announcing joins and quits that aren't their most honest.
 * Note that delayed announce methods can be called without checking
 * to see if it's enabled first. The methods confirm before doing anything
 * particularly stupid.
 */
public final class VanishAnnounceManipulator {
    private final Set<UUID> delayedAnnouncePlayerList;
    private final VanishPlugin plugin;
    private final Map<UUID, Boolean> playerOnlineStatus;

    VanishAnnounceManipulator(VanishPlugin plugin) {
        this.plugin = plugin;
        this.playerOnlineStatus = new HashMap<UUID, Boolean>();
        this.delayedAnnouncePlayerList = new HashSet<UUID>();
    }

    /**
     * Resolves an online player's name to their UUID
     *
     * @param playerName name of the player
     * @return the UUID or null if not online
     */
    private UUID uuid(String playerName) {
        final Player player = this.plugin.getServer().getPlayerExact(playerName);
        return player == null ? null : player.getUniqueId();
    }

    public void addToDelayedAnnounce(String player) {
        final UUID uuid = this.uuid(player);
        if (uuid != null) {
            this.addToDelayedAnnounce(uuid);
        }
    }

    public void addToDelayedAnnounce(UUID player) {
        this.playerOnlineStatus.put(player, false);
        if (!Settings.getAutoFakeJoinSilent()) {
            return;
        }
        this.delayedAnnouncePlayerList.add(player);
    }

    /**
     * Removes a player's delayed announce
     *
     * @param player name of the player
     */
    public void dropDelayedAnnounce(String player) {
        final UUID uuid = this.uuid(player);
        if (uuid != null) {
            this.dropDelayedAnnounce(uuid);
        }
    }

    public void dropDelayedAnnounce(UUID player) {
        this.delayedAnnouncePlayerList.remove(player);
    }

    /**
     * Gets the fake online status of a player
     * Called by JSONAPI
     *
     * @param playerName name of the player to query
     * @return true if player is considered online, false if not (or if not on server)
     */
    public boolean getFakeOnlineStatus(String playerName) {
        final Player player = this.plugin.getServer().getPlayerExact(playerName);
        if (player == null) {
            return false;
        }
        return this.playerOnlineStatus.getOrDefault(player.getUniqueId(), true);
    }

    /**
     * Marks a player as quit
     * Called when a player quits
     *
     * @param player name of the player who just quit
     * @return the former fake online status of the player
     */
    public boolean playerHasQuit(String player) {
        final UUID uuid = this.uuid(player);
        return uuid == null || this.playerHasQuit(uuid);
    }

    public boolean playerHasQuit(UUID player) {
        if (this.playerOnlineStatus.containsKey(player)) {
            return this.playerOnlineStatus.remove(player);
        }
        return true;
    }

    private String injectPlayerInformation(String message, Player player) {
        final VaultHook vault = (VaultHook) this.plugin.getHookManager().getHook(HookType.Vault);
        message = message.replace("%p", player.getName());
        message = message.replace("%d", LegacyComponentSerializer.legacySection().serialize(player.displayName()));
        String prefix = vault.getPrefix(player);
        message = message.replace("%up", prefix);
        String suffix = vault.getSuffix(player);
        message = message.replace("%us", suffix);
        return message;
    }

    private void broadcast(String message, Player player) {
        this.plugin.getServer().broadcast(Component.text().color(NamedTextColor.YELLOW).append(Messages.legacy(this.injectPlayerInformation(message, player))).build());
    }

    void fakeJoin(Player player, boolean force) {
        if (force || !(this.playerOnlineStatus.containsKey(player.getUniqueId()) && this.playerOnlineStatus.get(player.getUniqueId()))) {
            this.broadcast(Settings.getFakeJoin(), player);
            this.plugin.getLogger().info(player.getName() + " faked joining");
            this.playerOnlineStatus.put(player.getUniqueId(), true);
        }
    }

    void fakeQuit(Player player, boolean force) {
        if (force || !(this.playerOnlineStatus.containsKey(player.getUniqueId()) && !this.playerOnlineStatus.get(player.getUniqueId()))) {
            this.broadcast(Settings.getFakeQuit(), player);
            this.plugin.getLogger().info(player.getName() + " faked quitting");
            this.playerOnlineStatus.put(player.getUniqueId(), false);
        }
    }

    void vanishToggled(Player player) {
        if (!Settings.getAutoFakeJoinSilent() || !this.delayedAnnouncePlayerList.contains(player.getUniqueId())) {
            return;
        }
        this.fakeJoin(player, false);
        this.dropDelayedAnnounce(player.getUniqueId());
    }
}
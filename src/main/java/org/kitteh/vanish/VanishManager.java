package org.kitteh.vanish;

import com.google.common.collect.ImmutableSet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.vanish.event.VanishStatusChangeEvent;
import org.kitteh.vanish.utils.Messages;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VanishManager {
    public static final String VANISH_PLUGIN_CHANNEL = "vanishnopacket:status";

    private final VanishPlugin plugin;
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Boolean> sleepIgnored = new HashMap<UUID, Boolean>();
    private final Set<UUID> bats = new HashSet<UUID>();
    private final VanishAnnounceManipulator announceManipulator;
    private final Random random = new Random();

    public VanishManager(final VanishPlugin plugin) {
        this.plugin = plugin;
        this.announceManipulator = new VanishAnnounceManipulator(this.plugin);

        this.plugin.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, VanishManager.VANISH_PLUGIN_CHANNEL, new PluginMessageListener() {
            @Override
            public void onPluginMessageReceived(String channel, Player player, byte[] message) {
                if (channel.equals(VanishManager.VANISH_PLUGIN_CHANNEL) && new String(message).equals("check")) {
                    player.sendPluginMessage(plugin, VanishManager.VANISH_PLUGIN_CHANNEL, VanishManager.this.isVanished(player) ? new byte[]{0x01} : new byte[]{0x00});
                }
            }
        });
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, VanishManager.VANISH_PLUGIN_CHANNEL);

    }

    /**
     * Gets the announcement manipulator
     * Called by JSONAPI
     *
     * @return the Announce Manipulator
     */
    public VanishAnnounceManipulator getAnnounceManipulator() {
        return this.announceManipulator;
    }

    public Set<UUID> getBats() {
        return this.bats;
    }

    public Set<String> getVanishedPlayers() {
        final ImmutableSet.Builder<String> names = ImmutableSet.builder();
        for (final UUID uuid : this.vanishedPlayers) {
            final Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null) {
                names.add(player.getName());
            }
        }
        return names.build();
    }

    /**
     * Gets if a player is vanished
     *
     * @param player player to query
     * @return true if vanished
     */
    public boolean isVanished(Player player) {
        return this.vanishedPlayers.contains(player.getUniqueId());
    }

    /**
     * Gets if a player is vanished
     *
     * @param playerName name of the player to query
     * @return if the named player is currently vanished
     */
    public boolean isVanished(String playerName) {
        final Player player = this.plugin.getServer().getPlayerExact(playerName);
        if (player != null) {
            Debuggle.log("Testing vanished status of " + player.getName() + ": " + this.isVanished(player));
            return this.isVanished(player);
        }
        Debuggle.log("Testing vanished status of " + playerName + ": null");
        return false;
    }

    /**
     * Gets the number of vanished players
     *
     * @return the number of players currently vanished
     */
    public int numVanished() {
        return this.vanishedPlayers.size();
    }

    /**
     * Marks a player as having quit the game
     * Do not call this method
     *
     * @param player the player who has quit
     */
    public void playerQuit(Player player) {
        Debuggle.log("Quitting: " + player.getName());
        this.resetSleepingIgnored(player);
        if (this.isVanished(player)) {
            this.resetTraces(player);
        }
        VanishPerms.userQuit(player);
        this.vanishedPlayers.remove(player.getUniqueId());
    }

    /**
     * Resets a player's visibility status based on current permissions
     *
     * @param player player to refresh
     */
    public void playerRefresh(Player player) {
        this.resetSeeing(player);
        if (this.isVanished(player) && !VanishPerms.canVanish(player)) {
            this.toggleVanish(player);
        }
    }

    /**
     * Forces a visibility refresh on a player
     *
     * @param player player to refresh
     */
    public void resetSeeing(Player player) {
        Debuggle.log("Resetting visibility on " + player.getName());
        if (VanishPerms.canSeeAll(player)) {
            this.showVanished(player);
            Debuggle.log("Showing all to " + player.getName());
        } else {
            this.hideVanished(player);
            Debuggle.log("Hiding all to " + player.getName());
        }
    }

    /**
     * Toggles a player's visibility
     * Called when a player calls /vanish
     * Talks to the player and everyone with vanish.see
     * Will trigger effects
     *
     * @param togglingPlayer the player disappearing
     */
    public void toggleVanish(Player togglingPlayer) {
        this.toggleVanishQuiet(togglingPlayer);
        final String vanishingPlayerName = togglingPlayer.getName();
        final String messageBit;
        if (this.isVanished(togglingPlayer)) {
            Debuggle.log("LoudVanishToggle Vanishing " + togglingPlayer.getName());
            messageBit = "vanished. Poof.";

        } else {
            Debuggle.log("LoudVanishToggle Revealing " + togglingPlayer.getName());
            messageBit = "become visible.";
            this.announceManipulator.vanishToggled(togglingPlayer);
        }
        togglingPlayer.sendMessage(Messages.prefixed("You have " + messageBit));
        this.plugin.messageStatusUpdate(Messages.prefixed(Component.text(vanishingPlayerName + " has " + messageBit, NamedTextColor.YELLOW)), togglingPlayer);
    }

    /**
     * Toggles a player's visibility
     * Does not say anything.
     * Will trigger effects
     * Called by toggleVanish(Player)
     *
     * @param vanishingPlayer
     */
    public void toggleVanishQuiet(Player vanishingPlayer) {
        this.toggleVanishQuiet(vanishingPlayer, true);
    }

    /**
     * Toggles a player's visibility
     * Does not say anything.
     *
     * @param vanishingPlayer
     * @param effects if true, trigger effects
     */
    public void toggleVanishQuiet(Player vanishingPlayer, boolean effects) {
        final boolean vanishing = !this.isVanished(vanishingPlayer);
        final String vanishingPlayerName = vanishingPlayer.getName();
        if (vanishing) {
            Debuggle.log("It's invisible time! " + vanishingPlayer.getName());
            this.setSleepingIgnored(vanishingPlayer);
            if (VanishPerms.canNotFollow(vanishingPlayer)) {
                for (final Entity entity : vanishingPlayer.getNearbyEntities(100, 100, 100)) {
                    if (entity instanceof Mob mob && vanishingPlayer.equals(mob.getTarget())) {
                        mob.setTarget(null);
                    }
                    if (entity instanceof Warden warden) {
                        warden.clearAnger(vanishingPlayer);
                    }
                }
            }
            this.vanishedPlayers.add(vanishingPlayer.getUniqueId());
            this.applyTraces(vanishingPlayer);
            this.plugin.getLogger().info(vanishingPlayerName + " disappeared.");
            this.plugin.hooksVanish(vanishingPlayer);
        } else {
            Debuggle.log("It's visible time! " + vanishingPlayer.getName());
            this.resetSleepingIgnored(vanishingPlayer);
            this.vanishedPlayers.remove(vanishingPlayer.getUniqueId());
            this.resetTraces(vanishingPlayer);
            this.plugin.getLogger().info(vanishingPlayerName + " reappeared.");
            this.plugin.hooksUnvanish(vanishingPlayer);
        }
        if (effects) {
            final Location oneUp = vanishingPlayer.getLocation().add(0, 1, 0);
            if (VanishPerms.canEffectSmoke(vanishingPlayer)) {
                this.effectSmoke(vanishingPlayer.getLocation());
            }
            if (VanishPerms.canEffectExplode(vanishingPlayer)) {
                this.effectExplosion(vanishingPlayer);
            }
            if (VanishPerms.canEffectLightning(vanishingPlayer)) {
                this.effectLightning(vanishingPlayer.getLocation());
            }
            if (VanishPerms.canEffectFlames(vanishingPlayer)) {
                this.effectFlames(oneUp);
            }
            if (VanishPerms.canEffectBats(vanishingPlayer)) {
                this.effectBats(oneUp);
            }
            if (Settings.getSoundOnVanish()) {
                vanishingPlayer.playSound(vanishingPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5.0F, 5.0F);
            }
        }
        if(Settings.getVanishedActionBarMessage() != null && !Settings.getVanishedActionBarMessage().isEmpty()) {
            showVanishedActionBar(vanishingPlayer);
        }
        this.plugin.getServer().getPluginManager().callEvent(new VanishStatusChangeEvent(vanishingPlayer, vanishing));
        vanishingPlayer.sendPluginMessage(this.plugin, VanishManager.VANISH_PLUGIN_CHANNEL, vanishing ? new byte[] { 0x01 } : new byte[] { 0x00 });
        final java.util.Collection<? extends Player> playerList = this.plugin.getServer().getOnlinePlayers();
        for (final Player otherPlayer : playerList) {
            if (vanishingPlayer.equals(otherPlayer)) {
                continue;
            }
            Debuggle.log("Determining what to do about " + vanishingPlayer.getName() + " for " + otherPlayer.getName());
            if (!vanishing) {
                Debuggle.log("Showing " + vanishingPlayer.getName() + " to " + otherPlayer.getName());
                otherPlayer.showPlayer(this.plugin, vanishingPlayer);
            } else if (!VanishPerms.canSeeAll(otherPlayer)) {
                Debuggle.log("Hiding " + vanishingPlayer.getName() + " from " + otherPlayer.getName());
                otherPlayer.hidePlayer(this.plugin, vanishingPlayer);
            }
        }
    }

    private void showVanishedActionBar(Player vanishingPlayer) {
        if (Bukkit.getPlayer(vanishingPlayer.getUniqueId()) != null && isVanished(vanishingPlayer)) {
            vanishingPlayer.sendActionBar(Messages.legacy(Settings.getVanishedActionBarMessage()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    showVanishedActionBar(vanishingPlayer);
                }
            }.runTaskLater(plugin, 20);
        } else {
            vanishingPlayer.sendActionBar(Component.empty());
        }
    }

    /**
     * Vanishes a player. Poof.
     * This is a convenience method.
     *
     * @param vanishingPlayer player to hide
     * @param silent if true, does not say anything
     * @param effects if true, trigger effects
     */
    public void vanish(Player vanishingPlayer, boolean silent, boolean effects) {
        if (this.isVanished(vanishingPlayer)) {
            return;
        }
        if (silent) {
            this.toggleVanishQuiet(vanishingPlayer, effects);
        } else {
            this.toggleVanish(vanishingPlayer);
        }
    }

    /**
     * Reveals a player.
     * This is a convenience method.
     *
     * @param revealingPlayer player to reveal
     * @param silent if true, does not say anything
     * @param effects if true, trigger effects
     */
    public void reveal(Player revealingPlayer, boolean silent, boolean effects) {
        if (!this.isVanished(revealingPlayer)) {
            return;
        }
        if (silent) {
            this.toggleVanishQuiet(revealingPlayer, effects);
        } else {
            this.toggleVanish(revealingPlayer);
        }
    }

    private void effectBats(final Location location) {
        final Set<UUID> batty = new HashSet<UUID>();
        for (int x = 0; x < 10; x++) {
            batty.add(location.getWorld().spawnEntity(location, EntityType.BAT).getUniqueId());
        }
        this.bats.addAll(batty);
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                VanishManager.this.effectBatsCleanup(location.getWorld(), batty);
                VanishManager.this.bats.removeAll(batty);
            }
        }, 3 * 20);
    }

    private void effectBatsCleanup(World world, Set<UUID> bats) {
        for (final Entity entity : world.getEntities()) {
            if (bats.contains(entity.getUniqueId())) {
                world.playEffect(entity.getLocation(), Effect.SMOKE_SHOOT, this.random.nextInt(9));
                entity.remove();
            }
        }
    }

    private void effectExplosion(Player player) {
        Location loc = player.getLocation();
        player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0F, false, false);
    }

    private void effectFlames(Location location) {
        for (int i = 0; i < 10; i++) {
            location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, this.random.nextInt(9));
        }
    }

    private void effectLightning(Location location) {
        final int x = location.getBlockX();
        final double y = location.getBlockY();
        final int z = location.getBlockZ();
        for (int i = 0; i < Settings.getLightningCount(); i++) {
            double xToStrike;
            double zToStrike;
            if (this.random.nextBoolean()) {
                xToStrike = x + this.random.nextInt(6);
            } else {
                xToStrike = x - this.random.nextInt(6);
            }
            if (this.random.nextBoolean()) {
                zToStrike = z + this.random.nextInt(6);
            } else {
                zToStrike = z - this.random.nextInt(6);
            }
            final Location toStrike = new Location(location.getWorld(), xToStrike, y, zToStrike);
            location.getWorld().strikeLightningEffect(toStrike);
        }
    }

    private void effectSmoke(Location location) {
        for (int i = 0; i < 10; i++) {
            location.getWorld().playEffect(location, Effect.SMOKE_SHOOT, this.random.nextInt(9));
        }
    }

    private void hideVanished(Player player) {
        for (final Player otherPlayer : this.plugin.getServer().getOnlinePlayers()) {
            if (!player.equals(otherPlayer) && this.isVanished(otherPlayer)) {
                player.hidePlayer(this.plugin, otherPlayer);
            }
        }
    }

    private void showVanished(Player player) {
        for (final Player otherPlayer : this.plugin.getServer().getOnlinePlayers()) {
            if (!player.equals(otherPlayer) && this.isVanished(otherPlayer)) {
                player.showPlayer(this.plugin, otherPlayer);
            }
        }
    }

    /**
     * Mutes sounds made by the player (steps, falling, soul speed...) and stops pushing
     *
     * @param player the vanished player
     */
    void applyTraces(Player player) {
        player.setSilent(true);
        player.setCollidable(false);
    }

    /**
     * Undoes applyTraces, so silent is not saved into the player data
     *
     * @param player the formerly vanished player
     */
    void resetTraces(Player player) {
        player.setSilent(false);
        player.setCollidable(true);
    }

    void onPluginDisable() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (this.isVanished(player)) {
                this.resetTraces(player);
            }
        }
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            for (final Player player2 : this.plugin.getServer().getOnlinePlayers()) {
                if ((player != null) && (player2 != null) && !player.equals(player2)) {
                    player.showPlayer(plugin, player2);
                }
            }
        }
        for (final World world : this.plugin.getServer().getWorlds()) {
            this.effectBatsCleanup(world, this.bats);
        }
    }

    void resetSleepingIgnored(Player player) {
        if (this.sleepIgnored.containsKey(player.getUniqueId())) {
            player.setSleepingIgnored(this.sleepIgnored.remove(player.getUniqueId()));
        }
    }

    void setSleepingIgnored(Player player) {
        if (!this.sleepIgnored.containsKey(player.getUniqueId())) {
            this.sleepIgnored.put(player.getUniqueId(), player.isSleepingIgnored());
        }
        player.setSleepingIgnored(true);
    }
}
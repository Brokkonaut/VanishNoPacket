package org.kitteh.vanish;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.LazyMetadataValue.CacheStrategy;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.hooks.HookManager;
import org.kitteh.vanish.hooks.HookManager.HookType;
import org.kitteh.vanish.listeners.Listen119;
import org.kitteh.vanish.listeners.ListenEntity;
import org.kitteh.vanish.listeners.ListenInventory;
import org.kitteh.vanish.listeners.ListenPaperEvents;
import org.kitteh.vanish.listeners.ListenPlayerJoin;
import org.kitteh.vanish.listeners.ListenPlayerMessages;
import org.kitteh.vanish.listeners.ListenPlayerOther;
import org.kitteh.vanish.listeners.ListenToYourHeart;
import org.kitteh.vanish.utils.Messages;
import org.kitteh.vanish.utils.MinecraftVersion;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public final class VanishPlugin extends JavaPlugin {

    private final HashSet<UUID> haveInventoriesOpen = new HashSet<UUID>();
    private VanishManager manager;
    private final HookManager hookManager = new HookManager(this);

    /**
     * Informs VNP that a user has closed their fake chest
     *
     * @param name user's name
     * @deprecated names are not unique over time, use the UUID or Player variant
     */
    @Deprecated
    public void chestFakeClose(String name) {
        final Player player = this.getServer().getPlayerExact(name);
        if (player != null) {
            this.chestFakeClose(player);
        }
    }

    /**
     * Queries if a user is currently using a fake chest
     *
     * @param name the user's name
     * @return true if currently using a fake chest
     * @deprecated names are not unique over time, use the UUID or Player variant
     */
    @Deprecated
    public boolean chestFakeInUse(String name) {
        final Player player = this.getServer().getPlayerExact(name);
        return (player != null) && this.chestFakeInUse(player);
    }

    /**
     * Informs VNP that a user has opened their fake chest
     *
     * @param name user's name
     * @deprecated names are not unique over time, use the UUID or Player variant
     */
    @Deprecated
    public void chestFakeOpen(String name) {
        final Player player = this.getServer().getPlayerExact(name);
        if (player != null) {
            this.chestFakeOpen(player);
        }
    }

    /**
     * Informs VNP that a user has closed their fake chest
     *
     * @param player the user
     */
    public void chestFakeClose(HumanEntity player) {
        synchronized (this.haveInventoriesOpen) {
            this.haveInventoriesOpen.remove(player.getUniqueId());
        }
    }

    /**
     * Queries if a user is currently using a fake chest
     *
     * @param player the user
     * @return true if currently using a fake chest
     */
    public boolean chestFakeInUse(HumanEntity player) {
        synchronized (this.haveInventoriesOpen) {
            return this.haveInventoriesOpen.contains(player.getUniqueId());
        }
    }

    /**
     * Informs VNP that a user has opened their fake chest
     *
     * @param player the user
     */
    public void chestFakeOpen(HumanEntity player) {
        synchronized (this.haveInventoriesOpen) {
            this.haveInventoriesOpen.add(player.getUniqueId());
        }
    }

    /**
     * Gets the current version
     *
     * @return version of VanishNoPacket in use
     */
    public String getCurrentVersion() {
        return this.getPluginMeta().getVersion();
    }

    /**
     * Gets the hook manager
     *
     * @return the hook manager
     */
    public HookManager getHookManager() {
        return this.hookManager;
    }

    /**
     * Gets the vanish manager
     *
     * @return the VanishManager
     */
    public VanishManager getManager() {
        return this.manager;
    }

    /**
     * Indicates a player has just joined the server.
     * Internal use only please
     *
     * @param player player who has joined the server
     */
    public void hooksJoin(Player player) {
        this.hookManager.onJoin(player);
    }

    /**
     * Indicates a player has left the server
     * Internal use only please
     *
     * @param player player who has left the server
     */
    public void hooksQuit(Player player) {
        this.hookManager.onQuit(player);
        this.hookManager.onUnvanish(player);
    }

    /**
     * Calls hooks for when a player has unvanished
     * Internal use only please
     *
     * @param player the un-vanishing user
     */
    public void hooksUnvanish(Player player) {
        this.hookManager.onUnvanish(player);
    }

    /**
     * Calls hooks for when player has vanished
     * Internal use only please.
     *
     * @param player the vanishing player
     */
    public void hooksVanish(Player player) {
        this.hookManager.onVanish(player);
    }

    /**
     * Sends a message to all players with vanish.statusupdates permission
     *
     * @param message the message to send
     */
    public void messageStatusUpdate(String message) {
        this.messageStatusUpdate(message, null);
    }

    /**
     * Sends a message to all players with vanish.statusupdates but one
     *
     * @param message the message to send
     * @param avoid player to not send the message to
     */
    public void messageStatusUpdate(String message, Player avoid) {
        this.messageStatusUpdate(Messages.legacy(message), avoid);
    }

    /**
     * Sends a message to all players with vanish.statusupdates permission
     *
     * @param message the message to send
     */
    public void messageStatusUpdate(Component message) {
        this.messageStatusUpdate(message, null);
    }

    /**
     * Sends a message to all players with vanish.statusupdates but one
     *
     * @param message the message to send
     * @param avoid player to not send the message to
     */
    public void messageStatusUpdate(Component message, Player avoid) {
        for (final Player player : this.getServer().getOnlinePlayers()) {
            if ((player != null) && !player.equals(avoid) && VanishPerms.canSeeStatusUpdates(player)) {
                player.sendMessage(message);
            }
        }
    }

    @Override
    public void onDisable() {
        Debuggle.nah();
        for (final Player player : VanishPlugin.this.getServer().getOnlinePlayers()) {
            if (player != null) {
                if (this.manager.isVanished(player)) {
                    player.sendMessage(Messages.prefixed("You have been forced visible by a reload."));
                }
            }
        }
        this.hookManager.onDisable();
        this.manager.onPluginDisable();
        this.getLogger().info(this.getCurrentVersion() + " unloaded.");
    }

    @Override
    public void onEnable() {
        final File check = new File(this.getDataFolder(), "config.yml");
        if (!check.exists()) {
            this.saveDefaultConfig();
            this.reloadConfig();
            if (this.getServer().getPluginManager().isPluginEnabled("Essentials")) {
                this.getConfig().set("hooks.essentials", true);
                this.getLogger().info("Detected Essentials. Enabling Essentials hook.");
                this.saveConfig();
            }
        }

        Settings.freshStart(this);

        if (this.getConfig().getBoolean("hooks.essentials", false)) {
            this.hookManager.getHook(HookType.Essentials).onEnable();
        }
        if (this.getConfig().getBoolean("hooks.dynmap", false)) {
            this.hookManager.getHook(HookType.Dynmap).onEnable();
        }

        this.manager = new VanishManager(this);

        for (final Player player : this.getServer().getOnlinePlayers()) {
            player.setMetadata("vanished", new LazyMetadataValue(this, CacheStrategy.NEVER_CACHE, new VanishCheck(this.manager, player.getUniqueId())));
        }

        this.registerCommands(new VanishCommand(this));
        this.getServer().getPluginManager().registerEvents(new ListenEntity(this), this);
        this.getServer().getPluginManager().registerEvents(new ListenPlayerMessages(this), this);
        this.getServer().getPluginManager().registerEvents(new ListenPlayerJoin(this), this);
        this.getServer().getPluginManager().registerEvents(new ListenPlayerOther(this), this);
        this.getServer().getPluginManager().registerEvents(new ListenToYourHeart(this), this);
        this.getServer().getPluginManager().registerEvents(new ListenInventory(this), this);
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            this.getServer().getPluginManager().registerEvents(new ListenPaperEvents(this), this);
        } catch (ClassNotFoundException ignored) {
            this.getLogger().warning("Not using Paper API, because it is not available.");
        }
        if (MinecraftVersion.isAboveOrEqual(1, 19, 0)) {
            this.getServer().getPluginManager().registerEvents(new Listen119(this), this);
        }
        this.getLogger().info(this.getCurrentVersion() + " loaded.");
    }

    private void registerCommands(VanishCommand executor) {
        final Command command = new Command("vanish", "poof", "/vanish", List.of()) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                return executor.onCommand(sender, this, label, args);
            }
        };
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            // Brigadier aliases do not tell which alias was used, but VanishCommand acts on the label: one registration per label
            for (final String label : List.of("vanish", "v", "np", "nf", "ni", "nc", "nh")) {
                final BasicCommand basicCommand = (source, args) -> executor.onCommand(source.getSender(), command, label, args);
                event.registrar().register(label, "poof", basicCommand);
            }
        });
    }

    /**
     * Reloads the VNP config
     */
    public void reload() {
        this.reloadConfig();
        Settings.freshStart(this);
    }
}
package org.kitteh.vanish.listeners;

import java.util.HashSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.kitteh.vanish.Settings;
import org.kitteh.vanish.VanishPerms;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.utils.StringUtil;

public final class ListenPlayerOther implements Listener {
    private final VanishPlugin plugin;
    private final HashSet<Material> fakeInventoryBlockTypes;

    public ListenPlayerOther(VanishPlugin instance) {
        this.plugin = instance;
        this.fakeInventoryBlockTypes = new HashSet<>();
        // some of the materials might not exist in older minecraft versions, so order is important
        try {
            fakeInventoryBlockTypes.add(Material.CHEST);
            fakeInventoryBlockTypes.add(Material.TRAPPED_CHEST);
            fakeInventoryBlockTypes.add(Material.BARREL);
            fakeInventoryBlockTypes.add(Material.CHISELED_BOOKSHELF);
            fakeInventoryBlockTypes.add(Material.DECORATED_POT);
        } catch (NoSuchFieldError ignored) {
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canNotInteract(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canNotInteract(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (this.plugin.getManager().isVanished(player) && VanishPerms.canNotHunger(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!this.plugin.chestFakeInUse(player.getName()) && !player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_BLOCK) && this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canReadChestsSilently(event.getPlayer())) {
            final Block block = event.getClickedBlock();
            final BlockState blockState = block.getState();
            final Material blockType = block.getType();
            boolean fake = false;
            if (fakeInventoryBlockTypes.contains(blockType) || blockState instanceof ShulkerBox) {
                fake = true;
            }
            Inventory inventory = null;
            if (blockState instanceof BlockInventoryHolder blockInventoryHolder) {
                inventory = blockInventoryHolder.getInventory();
            } else if (blockType == Material.ENDER_CHEST) {
                if (this.plugin.getServer().getPluginManager().isPluginEnabled("EnderChestPlus") && VanishPerms.canNotInteract(player)) {
                    event.setCancelled(true);
                    return;
                }
                inventory = player.getEnderChest();
            }
            if (inventory != null) {
                event.setCancelled(true);
                if (fake) {
                    Inventory originalInventory = inventory;
                    inventory = this.plugin.getServer().createInventory(player, ((originalInventory.getSize() + 8) / 9) * 9, Component.text(StringUtil.capitalizeFirstLetter(blockType.name())));
                    inventory.setContents(originalInventory.getContents());
                    this.plugin.chestFakeOpen(player.getName());
                    player.sendMessage(Component.text("[VNP] Opening chest silently. Can not edit.", NamedTextColor.AQUA));
                }
                player.openInventory(inventory);
                return;
            }
        }
        if (this.plugin.getManager().isVanished(player) && VanishPerms.canNotInteract(player))

        {
            event.setCancelled(true);
            return;
        }
        if ((event.getAction() == Action.PHYSICAL) && (event.getClickedBlock().getType() == Material.FARMLAND)) {
            if (this.plugin.getManager().isVanished(player) && VanishPerms.canNotTrample(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canNotInteract(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player && this.plugin.getManager().isVanished((Player) event.getEntity()) && VanishPerms.canNotPickUp((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
        if (this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canNotPickUp(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (this.plugin.getManager().isVanished(player)) {
            this.plugin.messageStatusUpdate(Component.text(event.getPlayer().getName() + " has quit vanished", NamedTextColor.DARK_AQUA));
        }
        this.plugin.getManager().playerQuit(player);
        this.plugin.hooksQuit(player);
        this.plugin.getManager().getAnnounceManipulator().dropDelayedAnnounce(player.getName());
        if (!this.plugin.getManager().getAnnounceManipulator().playerHasQuit(player.getName()) || VanishPerms.silentQuit(player)) {
            event.quitMessage(null);
        }
        this.plugin.chestFakeClose(event.getPlayer().getName());
        player.removeMetadata("vanished", this.plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent event) {
        if (this.plugin.getManager().isVanished(event.getPlayer()) && VanishPerms.canNotInteract(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (Settings.getWorldChangeCheck()) {
            this.plugin.getManager().playerRefresh(event.getPlayer());
        }
    }
}
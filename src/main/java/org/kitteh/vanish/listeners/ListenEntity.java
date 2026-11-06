package org.kitteh.vanish.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.kitteh.vanish.VanishPerms;
import org.kitteh.vanish.VanishPlugin;

public final class ListenEntity implements Listener {
    private final VanishPlugin plugin;

    public ListenEntity(VanishPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        final Entity smacked = event.getEntity();
        if (this.plugin.getManager().getBats().contains(smacked.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (smacked instanceof Player) {
            final Player player = (Player) smacked;
            if (this.plugin.getManager().isVanished(player) && VanishPerms.blockIncomingDamage(player)) {
                event.setCancelled(true);
            }
        }
        if (event instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
            final Entity damager = ev.getDamager();
            Player player = null;
            if (damager instanceof Player) {
                player = (Player) damager;
            } else if (damager instanceof Projectile) {
                final Projectile projectile = (Projectile) damager;
                if ((projectile.getShooter() != null) && (projectile.getShooter() instanceof Player)) {
                    player = (Player) projectile.getShooter();
                }
            }
            if ((player != null) && this.plugin.getManager().isVanished(player) && VanishPerms.blockOutgoingDamage(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        // also covers EntityTargetLivingEntityEvent, any entity type (warden, ghast, phantom, slime, shulker, hoglin...)
        if ((event.getTarget() instanceof Player player) && this.plugin.getManager().isVanished(player) && VanishPerms.canNotFollow(player)) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Warden warden) {
                warden.clearAnger(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        final Entity entity = event.getAttacker();
        if ((entity instanceof Player) && this.plugin.getManager().isVanished((Player) event.getAttacker())) {
            if (VanishPerms.canNotInteract((Player) entity)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        if ((event.getEntity() instanceof Player) && this.plugin.getManager().isVanished((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        if ((event.getEntity() instanceof Player) && this.plugin.getManager().isVanished((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRaidTrigger(RaidTriggerEvent event) {
        if (this.plugin.getManager().isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
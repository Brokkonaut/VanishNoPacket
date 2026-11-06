package org.kitteh.vanish;

import org.bukkit.entity.Player;

/**
 * Permissions are checked live. A toggle flips the result relative to the permission.
 */
public final class VanishUser {
    private final Player player;
    private boolean seeAll;
    private boolean noPickup;
    private boolean noFollow;
    private boolean preventIncomingDamage;
    private boolean preventOutgoingDamage;
    private boolean noInteract;
    private boolean noHunger;
    private boolean noChat;
    private boolean silentChestReads;
    private boolean smoke;
    private boolean flames;
    private boolean explode;
    private boolean lightning;
    private boolean bats;

    public VanishUser(Player player) {
        this.player = player;
    }

    private boolean get(String permission, boolean toggled) {
        return this.player.hasPermission(permission) != toggled;
    }

    public boolean getEffectBats() {
        return this.get("vanish.effects.bats", this.bats);
    }

    public boolean getEffectExplode() {
        return this.get("vanish.effects.explode", this.explode);
    }

    public boolean getEffectFlames() {
        return this.get("vanish.effects.flames", this.flames);
    }

    public boolean getEffectLightning() {
        return this.get("vanish.effects.lightning", this.lightning);
    }

    public boolean getEffectSmoke() {
        return this.get("vanish.effects.smoke", this.smoke);
    }

    public boolean getNoChat() {
        return this.get("vanish.nochat", this.noChat);
    }

    public boolean getNoFollow() {
        return this.get("vanish.nofollow", this.noFollow);
    }

    public boolean getNoHunger() {
        return this.get("vanish.nohunger", this.noHunger);
    }

    public boolean getNoInteract() {
        return this.get("vanish.nointeract", this.noInteract);
    }

    public boolean getNoPickup() {
        return this.get("vanish.nopickup", this.noPickup);
    }

    public boolean getPreventIncomingDamage() {
        return this.get("vanish.preventincomingdamage", this.preventIncomingDamage);
    }

    public boolean getPreventOutgoingDamage() {
        return this.get("vanish.preventoutgoingdamage", this.preventOutgoingDamage);
    }

    public boolean getReadChestsSilently() {
        return this.get("vanish.silentchests", this.silentChestReads);
    }

    public boolean getSeeAll() {
        return this.get("vanish.see", this.seeAll);
    }

    public boolean toggleEffectBats() {
        this.bats = !this.bats;
        return this.getEffectBats();
    }

    public boolean toggleEffectExplode() {
        this.explode = !this.explode;
        return this.getEffectExplode();
    }

    public boolean toggleEffectFlames() {
        this.flames = !this.flames;
        return this.getEffectFlames();
    }

    public boolean toggleEffectLightning() {
        this.lightning = !this.lightning;
        return this.getEffectLightning();
    }

    public boolean toggleEffectSmoke() {
        this.smoke = !this.smoke;
        return this.getEffectSmoke();
    }

    public boolean toggleIncomingDamage() {
        this.preventIncomingDamage = !this.preventIncomingDamage;
        return this.getPreventIncomingDamage();
    }

    public boolean toggleNoChat() {
        this.noChat = !this.noChat;
        return this.getNoChat();
    }

    public boolean toggleNoFollow() {
        this.noFollow = !this.noFollow;
        return this.getNoFollow();
    }

    public boolean toggleNoHunger() {
        this.noHunger = !this.noHunger;
        return this.getNoHunger();
    }

    public boolean toggleNoInteract() {
        this.noInteract = !this.noInteract;
        return this.getNoInteract();
    }

    public boolean toggleNoPickup() {
        this.noPickup = !this.noPickup;
        return this.getNoPickup();
    }

    public boolean toggleOutgoingDamage() {
        this.preventOutgoingDamage = !this.preventOutgoingDamage;
        return this.getPreventOutgoingDamage();
    }

    public boolean toggleSeeAll() {
        this.seeAll = !this.seeAll;
        return this.getSeeAll();
    }

    public boolean toggleSilentChestReads() {
        this.silentChestReads = !this.silentChestReads;
        return this.getReadChestsSilently();
    }
}

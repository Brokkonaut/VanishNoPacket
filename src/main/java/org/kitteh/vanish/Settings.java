package org.kitteh.vanish;

import org.bukkit.configuration.file.FileConfiguration;

public final class Settings {
    private static boolean enablePermTest;
    private static String fakeQuit;
    private static String fakeJoin;
    private static boolean autoFakeJoinSilent;
    private static boolean worldChangeCheck;
    private static int lightningEffectCount;
    private static boolean soundOnVanish;
    private static String vanishedActionBarMessage;

    private static final int confVersion = 7; // Tracking config version

    public static boolean getAutoFakeJoinSilent() {
        return Settings.autoFakeJoinSilent;
    }

    public static boolean getEnablePermTest() {
        return Settings.enablePermTest;
    }

    public static String getFakeJoin() {
        return Settings.fakeJoin;
    }

    public static String getFakeQuit() {
        return Settings.fakeQuit;
    }

    public static int getLightningCount() {
        return Settings.lightningEffectCount;
    }

    public static boolean getWorldChangeCheck() {
        return Settings.worldChangeCheck;
    }

    public static boolean getSoundOnVanish() {
        return Settings.soundOnVanish;
    }

    public static String getVanishedActionBarMessage() {
        return vanishedActionBarMessage;
    }

    static void freshStart(VanishPlugin plugin) {
        final FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        final int ver = config.getInt("configVersionDoNotTouch.SeriouslyThisWillEraseYourConfig", 0);
        if (ver != Settings.confVersion) {
            plugin.getLogger().info("Attempting to update your configuration. Check to make sure it's ok");
            if (ver < 1) {
                config.set("spoutcraft.enable", null);
                config.set("spoutcraft", null);
            }
            if ((ver <= 1) || config.contains("permtest.enable")) {
                final boolean permtest = config.getBoolean("permtest.enable", false);
                config.set("permtest.enable", null);
                config.set("permtest", permtest);
                config.set("enableColoration", null);
                config.set("enableTabControl", null);
                config.set("updates.check", null);
            }
            if ((ver <= 3)) {
                config.set("effects.lightning.count", 30);
            }
            if ((ver <= 4)) {
            }
            if ((ver <= 5)) {
                config.set("soundOnVanish", true);
            }
            if ((ver <= 6)) {
                config.set("soundonvanish", null);
                config.set("checkupdates", null);
                config.set("updates", null);
                config.set("colornametags", null);
                config.set("hooks.spoutcraft", null);
                config.set("hooks.JSONAPI", null);
                config.set("vanishedActionBarMessage", config.getString("vanishedactionbarmessage", "&&3&&lYou are vanished!"));
                config.set("vanishedactionbarmessage", null);
            }
            config.set("configVersionDoNotTouch.SeriouslyThisWillEraseYourConfig", Settings.confVersion);
            plugin.saveConfig();
        }
        Settings.enablePermTest = config.getBoolean("permtest", false);
        Settings.fakeJoin = config.getString("fakeannounce.join", "%p joined the game.");
        Settings.fakeQuit = config.getString("fakeannounce.quit", "%p left the game.");
        Settings.autoFakeJoinSilent = config.getBoolean("fakeannounce.automaticforsilentjoin", false);
        Settings.worldChangeCheck = config.getBoolean("permissionsupdates.checkonworldchange", false);
        Settings.lightningEffectCount = config.getInt("effects.lightning.count", 30);
        if (Settings.lightningEffectCount < 1) {
            Settings.lightningEffectCount = 1;
        }
        Settings.soundOnVanish = config.getBoolean("soundOnVanish", true);
        Settings.vanishedActionBarMessage = config.getString("vanishedActionBarMessage", "&&3&&lYou are vanished!");
        if (config.getBoolean("debug", false)) {
            Debuggle.itsGoTime(plugin);
        } else {
            Debuggle.nah();
        }
    }
}
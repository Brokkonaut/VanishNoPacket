package org.kitteh.vanish.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Messages {
    private static final Component PREFIX = Component.text("[VNP] ", NamedTextColor.AQUA);

    private Messages() {
        throw new UnsupportedOperationException(); // prevents instances
    }

    /**
     * Prefixes a message with [VNP]. Parts without own color are DARK_AQUA.
     *
     * @param parts message parts
     * @return the prefixed message
     */
    public static Component prefixed(Component... parts) {
        return PREFIX.append(Component.text().color(NamedTextColor.DARK_AQUA).append(parts).build());
    }

    public static Component prefixed(String text) {
        return Messages.prefixed(Component.text(text));
    }

    /**
     * Converts a string with section sign color codes into a component
     *
     * @param legacy the string
     * @return the component
     */
    public static Component legacy(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }
}

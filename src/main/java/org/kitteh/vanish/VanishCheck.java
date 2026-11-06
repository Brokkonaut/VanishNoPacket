package org.kitteh.vanish;

import java.util.UUID;
import java.util.concurrent.Callable;

public final class VanishCheck implements Callable<Object> {
    private final VanishManager manager;
    private final UUID playerId;
    private final String name;

    public VanishCheck(VanishManager manager, UUID playerId) {
        this.manager = manager;
        this.playerId = playerId;
        this.name = null;
    }

    /**
     * @deprecated names are not unique over time, use {@link #VanishCheck(VanishManager, UUID)}
     */
    @Deprecated
    public VanishCheck(VanishManager manager, String name) {
        this.manager = manager;
        this.playerId = null;
        this.name = name;
    }

    @Override
    public Object call() {
        try {
            return this.playerId != null ? this.manager.isVanished(this.playerId) : this.manager.isVanished(this.name);
        } catch (final Exception e) {
            return false;
        }
    }
}

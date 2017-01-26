/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.events;

import io.github.nucleuspowered.nucleus.api.events.NucleusHomeEvent;
import io.github.nucleuspowered.nucleus.internal.event.AbstractCancelMessageEvent;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;

public abstract class AbstractHomeEvent extends AbstractCancelMessageEvent implements NucleusHomeEvent {

    private final String name;
    private final User owner;

    private boolean isCancelled = false;

    AbstractHomeEvent(String name, User owner, Cause cause) {
        super(cause);
        this.name = name;
        this.owner = owner;
    }

    @Override public String getName() {
        return name;
    }

    @Override public User getOwner() {
        return this.owner;
    }

    @Override public boolean isCancelled() {
        return isCancelled;
    }

    @Override public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}

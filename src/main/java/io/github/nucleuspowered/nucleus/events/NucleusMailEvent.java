/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.events;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class NucleusMailEvent extends AbstractEvent implements io.github.nucleuspowered.nucleus.api.events.MailEvent {

    private final User from;
    private final User to;
    private final String message;
    private boolean cancelled = false;

    public NucleusMailEvent(User from, User to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Cause getCause() {
        return Cause.of(NamedCause.source(from));
    }

    @Override
    public User getSender() {
        return from;
    }

    @Override
    public User getRecipient() {
        return to;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

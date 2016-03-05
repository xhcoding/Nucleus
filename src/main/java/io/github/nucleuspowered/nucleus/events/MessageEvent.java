/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.events;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class MessageEvent extends AbstractEvent implements Cancellable {

    private final CommandSource from;
    private final CommandSource to;
    private final String message;
    private boolean isCancelled = false;

    public MessageEvent(CommandSource from, CommandSource to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    @Override
    public Cause getCause() {
        return Cause.of(from);
    }

    public CommandSource getSender() {
        return from;
    }

    public CommandSource getRecipient() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}

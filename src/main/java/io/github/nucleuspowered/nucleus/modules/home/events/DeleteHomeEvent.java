/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.events;

import io.github.nucleuspowered.nucleus.api.events.NucleusHomeEvent;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.annotation.Nullable;

public class DeleteHomeEvent extends AbstractHomeEvent implements NucleusHomeEvent.Delete {

    @Nullable private final Location<World> location;

    public DeleteHomeEvent(String name, User owner, Cause cause, Location<World> location) {
        super(name, owner, cause);
        this.location = location;
    }

    @Override public Optional<Location<World>> getHomeLocation() {
        return Optional.ofNullable(this.location);
    }
}

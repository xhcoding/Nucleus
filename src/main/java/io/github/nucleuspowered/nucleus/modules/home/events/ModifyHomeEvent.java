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

public class ModifyHomeEvent extends AbstractHomeEvent implements NucleusHomeEvent.Modify {

    private final Location<World> newLocation;
    @Nullable private final Location<World> oldLocation;

    public ModifyHomeEvent(String name, User owner, Cause cause, @Nullable Location<World> oldLocation, Location<World> newLocation) {
        super(name, owner, cause);
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
    }

    @Override public Optional<Location<World>> getHomeLocation() {
        return Optional.ofNullable(oldLocation);
    }

    @Override public Location<World> getNewHomeLocation() {
        return newLocation;
    }
}

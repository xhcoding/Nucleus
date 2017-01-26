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

public class UseHomeEvent extends AbstractHomeEvent implements NucleusHomeEvent.Use {

    private final Location<World> location;
    private final User targetUser;

    public UseHomeEvent(String name, User owner, Cause cause, User targetUser, Location<World> location) {
        super(name, owner, cause);
        this.targetUser = targetUser;
        this.location = location;
    }

    @Override public Location<World> getHomeLocation() {
        return location;
    }

    @Override public User getTargetUser() {
        return targetUser;
    }
}

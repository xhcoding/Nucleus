/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.event;

import io.github.nucleuspowered.nucleus.api.events.NucleusWarpEvent;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class UseWarpEvent extends AbstractWarpEvent implements NucleusWarpEvent.Use {

    private final User user;
    private final Location<World> location;

    public UseWarpEvent(Cause cause, String name, User user, Location<World> worldLocation) {
        super(cause, name);
        this.user = user;
        this.location = worldLocation;
    }

    @Override public Location<World> getLocation() {
        return location;
    }

    @Override public User getTargetUser() {
        return user;
    }
}

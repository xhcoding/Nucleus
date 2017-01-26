/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.events;

import io.github.nucleuspowered.nucleus.api.data.Home;
import io.github.nucleuspowered.nucleus.api.events.NucleusHomeEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ModifyHomeEvent extends AbstractHomeEvent implements NucleusHomeEvent.Modify {

    private final Location<World> newLocation;
    private final Home home;

    public ModifyHomeEvent(Cause cause, Home home, Location<World> newLocation) {
        super(home.getName(), home.getUser(), cause, home.getLocation().orElse(null));
        this.newLocation = newLocation;
        this.home = home;
    }

    @Override public Home getHome() {
        return this.home;
    }

    @Override public Location<World> getNewHomeLocation() {
        return newLocation;
    }
}

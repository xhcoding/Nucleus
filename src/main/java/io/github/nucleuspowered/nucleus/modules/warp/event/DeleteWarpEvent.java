/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.event;

import io.github.nucleuspowered.nucleus.api.events.NucleusWarpEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
public class DeleteWarpEvent extends AbstractWarpEvent implements NucleusWarpEvent.Delete {

    @Nullable private final Location<World> oldLocation;

    public DeleteWarpEvent(Cause cause, String name, @Nullable Location<World> oldLocation) {
        super(cause, name);
        this.oldLocation = oldLocation;
    }

    @Override public Optional<Location<World>> getLocation() {
        return Optional.ofNullable(oldLocation);
    }
}

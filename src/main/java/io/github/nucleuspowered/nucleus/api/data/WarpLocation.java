/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.data;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.MessageFormat;

public class WarpLocation extends LocationWithRotation {

    private final String warpName;

    public WarpLocation(String name, Location<World> location, Vector3d rotation) {
        super(location, rotation);
        this.warpName = name;
    }

    public String getName() {
        return warpName;
    }

}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.config;

import com.flowpowered.math.GenericMath;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class RTPConfig {

    @Setting(value = "attempts", comment = "config.rtp.attempts")
    private int noOfAttempts = 10;

    @Setting(value = "radius", comment = "config.rtp.radius")
    private int radius = 30000;

    @Setting(value = "minimum-y", comment = "config.rtp.min-y")
    private int minY = 0;

    @Setting(value = "maximum-y", comment = "config.rtp.max-y")
    private int maxY = 255;

    @Setting(value = "surface-only", comment = "config.rtp.surface")
    private boolean mustSeeSky = false;

    @Setting(value = "per-world-permissions", comment = "config.rtp.perworldperms")
    private boolean perWorldPermissions = false;

    public int getNoOfAttempts() {
        return noOfAttempts;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isMustSeeSky() {
        return mustSeeSky;
    }

    public int getMinY() {
        return GenericMath.clamp(minY, 0, Math.min(255, maxY));
    }

    public int getMaxY() {
        // We use 252 as the safe TP handler might try to look above.
        return GenericMath.clamp(maxY, Math.max(0, minY), 255);
    }

    public boolean isPerWorldPermissions() {
        return perWorldPermissions;
    }
}

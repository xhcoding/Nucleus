/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class RTPConfig {

    @Setting(value = "attempts", comment = "loc:config.rtp.attempts")
    private int noOfAttempts = 10;

    @Setting(value = "radius", comment = "loc:config.rtp.radius")
    private int radius = 30000;

    @Setting(value = "surface-only", comment = "loc:config.rtp.surface")
    private boolean mustSeeSky = false;

    public int getNoOfAttempts() {
        return noOfAttempts;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isMustSeeSky() {
        return mustSeeSky;
    }
}

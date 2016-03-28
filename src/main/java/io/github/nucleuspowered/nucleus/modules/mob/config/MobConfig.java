/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mob.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MobConfig {

    @Setting(value = "max-mobs-to-spawn", comment = "loc:config.mobspawn.maxamt")
    private int maxMobsToSpawn = 20;

    public int getMaxMobsToSpawn() {
        return Math.max(1, maxMobsToSpawn);
    }
}

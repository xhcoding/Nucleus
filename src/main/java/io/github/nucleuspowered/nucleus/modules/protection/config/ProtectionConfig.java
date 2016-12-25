/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.protection.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ProtectionConfig {

    @Setting(value = "disable-crop-trample", comment = "loc:config.protection.disablecrop")
    private CropTrample disableCropTrample = new CropTrample();

    public boolean isDisableAnyCropTrample() {
        return disableCropTrample.players || disableCropTrample.mobs;
    }

    public boolean isDisablePlayerCropTrample() {
        return disableCropTrample.players;
    }

    public boolean isDisableMobCropTrample() {
        return disableCropTrample.mobs;
    }

    @ConfigSerializable
    public static class CropTrample {

        @Setting
        private boolean players = false;

        @Setting
        private boolean mobs = false;
    }
}

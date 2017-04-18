/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connection.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

@ConfigSerializable
public class ConnectionConfig {

    @Setting(value = "reserved-slots", comment = "config.connection.reservedslots")
    private int reservedSlots = -1;

    @Setting(value = "whitelist-message", comment = "config.connection.whitelistmessage")
    private String whitelistMessage = "";

    private Text cache = null;

    public int getReservedSlots() {
        return reservedSlots;
    }

    public Optional<Text> getWhitelistMessage() {
        if (whitelistMessage == null || whitelistMessage.isEmpty()) {
            return Optional.empty();
        }

        if (cache == null) {
            cache = TextSerializers.FORMATTING_CODE.deserializeUnchecked(whitelistMessage);
        }

        return Optional.of(this.cache);
    }
}

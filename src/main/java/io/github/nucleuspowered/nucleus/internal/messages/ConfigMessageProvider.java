/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.messages;

import io.github.nucleuspowered.nucleus.config.MessageConfig;

import java.util.Optional;

public class ConfigMessageProvider extends ResourceMessageProvider {

    private final MessageConfig mc;

    public ConfigMessageProvider(MessageConfig mc) {
        this.mc = mc;
    }

    @Override
    public Optional<String> getMessageFromKey(String key) {
        Optional<String> s = mc.getKey(key);
        if (s.isPresent()) {
            return s;
        }

        return super.getMessageFromKey(key);
    }
}

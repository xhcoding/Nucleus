/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal;

import com.google.common.collect.Maps;
import io.github.essencepowered.essence.config.AbstractConfig;
import io.github.essencepowered.essence.config.CommandsConfig;
import io.github.essencepowered.essence.config.MainConfig;
import io.github.essencepowered.essence.config.WarpsConfig;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ConfigMap {

    private final Map<Key, AbstractConfig> configMap = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> Optional<T> getConfig(Key<T> key) {
        return Optional.ofNullable((T)configMap.get(key));
    }

    public <T extends AbstractConfig> void putConfig(Key key, T config) {
        configMap.put(key, config);
    }

    public void reloadAll() {
        configMap.forEach((k, v) -> {
            try {
                v.load();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        });
    }

    public static class Key<V extends AbstractConfig> { }

    public static final Key<MainConfig> MAIN_CONFIG = new Key<>();
    public static final Key<CommandsConfig> COMMANDS_CONFIG = new Key<>();
    public static final Key<WarpsConfig> WARPS_CONFIG = new Key<>();
    public static final Key<WarpsConfig> JAILS_CONFIG = new Key<>();
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.config.WarpsConfig;
import io.github.nucleuspowered.nucleus.config.bases.AbstractStandardNodeConfig;

import java.util.Map;
import java.util.Optional;

public class ConfigMap {

    private final Map<Key, AbstractStandardNodeConfig> configMap = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <T extends AbstractStandardNodeConfig> Optional<T> getConfig(Key<T> key) {
        return Optional.ofNullable((T)configMap.get(key));
    }

    public <T extends AbstractStandardNodeConfig> void putConfig(Key key, T config) {
        configMap.put(key, config);
    }

    public void reloadAll() {
        configMap.forEach((k, v) -> {
            try {
                v.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static class Key<V extends AbstractStandardNodeConfig> { }

    public static final Key<CommandsConfig> COMMANDS_CONFIG = new Key<>();
    public static final Key<WarpsConfig> WARPS_CONFIG = new Key<>();
    public static final Key<WarpsConfig> JAILS_CONFIG = new Key<>();
    public static final Key<KitsConfig> KITS_CONFIG = new Key<>();
}

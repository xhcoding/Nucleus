/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import io.github.nucleuspowered.nucleus.Nucleus;

import java.util.Map;
import java.util.Optional;

public final class InternalServiceManager {

    private final Nucleus plugin;
    private Map<Class<?>, Object> serviceMap = Maps.newConcurrentMap();

    public InternalServiceManager(Nucleus plugin) {
        this.plugin = plugin;
    }

    public <I, C extends I> boolean registerService(Class<I> key, C service) {
        if (serviceMap.containsKey(key)) {
            return false;
        }

        serviceMap.put(key, service);
        plugin.updateInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(key).toInstance(service);
            }
        });

        return true;
    }

    @SuppressWarnings("unchecked")
    public <I> Optional<I> getService(Class<I> key) {
        if (serviceMap.containsKey(key)) {
            return Optional.of((I)serviceMap.get(key));
        }

        return Optional.empty();
    }
}

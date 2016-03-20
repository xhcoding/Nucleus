/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

public final class InternalServiceManager {

    private Map<Class<?>, Object> serviceMap = Maps.newConcurrentMap();

    public <I, C extends I> boolean registerService(Class<I> key, C service) {
        if (serviceMap.containsKey(key)) {
            return false;
        }

        serviceMap.put(key, service);
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

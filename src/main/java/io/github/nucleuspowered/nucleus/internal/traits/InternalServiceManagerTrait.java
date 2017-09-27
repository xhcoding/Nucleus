/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.traits;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;

import java.util.Optional;

public interface InternalServiceManagerTrait {

    default InternalServiceManager getServiceManager() {
        return Nucleus.getNucleus().getInternalServiceManager();
    }

    default <T> Optional<T> getService(Class<T> service) {
        return getServiceManager().getService(service);
    }

    default <T> T getServiceUnchecked(Class<T> service) {
        return getServiceManager().getServiceUnchecked(service);
    }
}

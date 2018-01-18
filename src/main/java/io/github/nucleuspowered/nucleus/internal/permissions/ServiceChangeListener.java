/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.permissions;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

import java.util.List;

public class ServiceChangeListener {

    public static ServiceChangeListener getInstance() {
        return INSTANCE;
    }

    private ServiceChangeListener() {
        Sponge.getEventManager().registerListeners(Nucleus.getNucleus(), this);
    }

    public void registerCalculator(ContextCalculator<Subject> contextCalculator) {
        this.contextCalculators.add(contextCalculator);
        Sponge.getServiceManager().provide(PermissionService.class).ifPresent(x -> x.registerContextCalculator(contextCalculator));
    }

    private final static ServiceChangeListener INSTANCE = new ServiceChangeListener();
    private final List<ContextCalculator<Subject>> contextCalculators = Lists.newArrayList();

    @Listener(order = Order.POST)
    public void onServiceChange(ChangeServiceProviderEvent event) {
        if (event.getService().isInstance(PermissionService.class)) {
            for (ContextCalculator<Subject> contextCalculator : this.contextCalculators) {
                ((PermissionService) event.getNewProvider()).registerContextCalculator(contextCalculator);
            }
        }
    }
}

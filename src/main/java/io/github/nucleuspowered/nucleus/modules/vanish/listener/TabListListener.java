/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.listener;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.service.VanishService;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class TabListListener extends ListenerBase implements ListenerBase.Conditional {

    @Listener(order = Order.POST)
    public void onLogin(ClientConnectionEvent.Join event) {
        getServiceUnchecked(VanishService.class).resetPlayerTabLists();
    }

    @Listener(order = Order.POST)
    public void onLogout(ClientConnectionEvent.Disconnect event) {
        getServiceUnchecked(VanishService.class).resetPlayerTabLists();
    }

    @Override
    public boolean shouldEnable() {
        return Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(VanishConfigAdapter.class).getNodeOrDefault().isAlterTabList();
    }
}

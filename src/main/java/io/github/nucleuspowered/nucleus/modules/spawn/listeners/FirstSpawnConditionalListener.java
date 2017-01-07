/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.spawn.SpawnModule;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.function.Predicate;

@ConditionalListener(FirstSpawnConditionalListener.Condition.class)
public class FirstSpawnConditionalListener extends ListenerBase {

    @Inject private GeneralService store;

    @Listener(order = Order.LATE)
    public void onJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        if (Util.isFirstPlay(player)) {
            // Try to force a player location.
            store.getFirstSpawn().ifPresent(player::setTransform);
        }
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            try {
                return nucleus.getModuleContainer().getConfigAdapterForModule(SpawnModule.ID, SpawnConfigAdapter.class)
                    .getNodeOrDefault().isForceFirstSpawn();
            } catch (Exception e) {
                if (nucleus.isDebugMode()) {
                    e.printStackTrace();
                }

                return false;
            }
        }
    }
}

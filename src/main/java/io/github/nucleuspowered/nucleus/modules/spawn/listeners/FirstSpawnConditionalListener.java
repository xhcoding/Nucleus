/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.spawn.SpawnModule;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfig;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnGeneralDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;

public class FirstSpawnConditionalListener extends ListenerBase implements ListenerBase.Conditional {

    private final ModularGeneralService store;

    @Inject
    public FirstSpawnConditionalListener(ModularGeneralService store) {
        this.store = store;
    }

    @Listener(order = Order.LATE)
    public void onJoin(NucleusFirstJoinEvent event, @Getter("getTargetEntity") Player player) {
        // Try to force a subject location in a tick.
        Task.builder().execute(() -> store.get(SpawnGeneralDataModule.class).getFirstSpawn().ifPresent(player::setTransform)).delayTicks(3)
                .submit(plugin);
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(SpawnModule.ID, SpawnConfigAdapter.class, SpawnConfig::isForceFirstSpawn).orElse(false);
    }

}

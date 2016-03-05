/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.CancellableTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public abstract class CostCancellableTask implements CancellableTask {

    private final double cost;
    private final Nucleus plugin;
    private final Player player;

    public CostCancellableTask(Nucleus plugin, Player src, double cost) {
        this.plugin = plugin;
        this.player = src;
        this.cost = cost;
    }

    @Override
    public void onCancel() {
        if (cost > 0) {
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> plugin.getEconHelper().depositInPlayer(player, cost));
        }
    }
}

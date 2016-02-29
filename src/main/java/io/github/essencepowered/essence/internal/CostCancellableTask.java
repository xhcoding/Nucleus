/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal;

import io.github.essencepowered.essence.QuickStart;
import io.github.essencepowered.essence.internal.interfaces.CancellableTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public abstract class CostCancellableTask implements CancellableTask {

    private final double cost;
    private final QuickStart plugin;
    private final Player player;

    public CostCancellableTask(QuickStart plugin, Player src, double cost) {
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

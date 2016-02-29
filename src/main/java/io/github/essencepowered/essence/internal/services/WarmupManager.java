/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal.services;

import com.google.common.collect.Maps;
import io.github.essencepowered.essence.api.service.EssenceWarmupManagerService;
import io.github.essencepowered.essence.internal.interfaces.CancellableTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class WarmupManager implements EssenceWarmupManagerService {

    private final Map<UUID, Task> warmupTasks = Maps.newHashMap();

    @Override
    public void addWarmup(UUID player, Task task) {
        Task t = warmupTasks.put(player, task);
        if (t != null) {
            t.cancel();

            Consumer<Task> ct = t.getConsumer();
            if (ct instanceof CancellableTask) {
                ((CancellableTask) ct).onCancel();
            }
        }
    }

    @Override
    public boolean removeWarmup(UUID player) {
        Task t = warmupTasks.remove(player);
        return t != null && t.cancel();
    }

    @Override
    public void cleanup() {
        warmupTasks.entrySet().stream().filter(v -> !Sponge.getGame().getScheduler().getScheduledTasks().contains(v.getValue()))
                .forEach(v -> warmupTasks.remove(v.getKey()));
    }
}

/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.runnables;

import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.TaskBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.services.TeleportHandler;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;

@Modules(PluginModule.TELEPORT)
public class TeleportTask extends TaskBase {
    @Inject private TeleportHandler handler;

    @Override
    public void accept(Task task) {
        handler.clearExpired();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public int secondsPerRun() {
        return 2;
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.runnables;

import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;

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

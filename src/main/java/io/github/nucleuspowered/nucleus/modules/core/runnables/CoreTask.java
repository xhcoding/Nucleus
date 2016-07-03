/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.runnables;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import org.spongepowered.api.scheduler.Task;

/**
 * Core tasks. No module, must always run.
 */
public class CoreTask extends TaskBase {
    @Inject private Nucleus plugin;
    @Inject private UserDataManager uda;

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public int secondsPerRun() {
        return 300;
    }

    @Override
    public void accept(Task task) {
        plugin.saveData();
        uda.removeOfflinePlayers();
    }
}

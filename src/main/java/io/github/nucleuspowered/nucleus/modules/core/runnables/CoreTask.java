/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.runnables;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Core tasks. No module, must always run.
 */
@NonnullByDefault
public class CoreTask extends TaskBase {

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(5, ChronoUnit.MINUTES);
    }

    @Override
    public void accept(Task task) {
        Nucleus plugin = Nucleus.getNucleus();
        if (Nucleus.getNucleus().isDebugMode()) {
            plugin.getLogger().info(plugin.getMessageProvider().getMessageWithFormat("core.savetask.starting"));
        }

        plugin.saveData();
        plugin.getUserDataManager().removeOfflinePlayers();

        if (Nucleus.getNucleus().isDebugMode()) {
            plugin.getLogger().info(plugin.getMessageProvider().getMessageWithFormat("core.savetask.complete"));
        }
    }
}

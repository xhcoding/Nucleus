package uk.co.drnaylor.minecraft.quickstart.runnables;

import org.spongepowered.api.scheduler.Task;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.TaskBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.services.TeleportHandler;

import javax.inject.Inject;

@Modules(PluginModule.TELEPORT)
public class TeleportTask implements TaskBase {
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

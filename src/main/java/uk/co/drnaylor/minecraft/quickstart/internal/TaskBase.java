package uk.co.drnaylor.minecraft.quickstart.internal;

import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

public interface TaskBase extends Consumer<Task> {

    boolean isAsync();

    int secondsPerRun();
}

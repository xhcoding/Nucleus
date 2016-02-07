package uk.co.drnaylor.minecraft.quickstart.internal.interfaces;

import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

public interface CancellableTask extends Consumer<Task> {

    void onCancel();
}

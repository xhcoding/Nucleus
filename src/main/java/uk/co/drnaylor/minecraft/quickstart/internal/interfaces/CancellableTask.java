/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal.interfaces;

import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

/**
 * Represents a task that has actions to perform when it's cancelled.
 */
public interface CancellableTask extends Consumer<Task> {

    /**
     * The actions to perform upon cancellation.
     */
    void onCancel();
}

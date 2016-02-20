/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal;

import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

public interface TaskBase extends Consumer<Task> {

    boolean isAsync();

    int secondsPerRun();
}

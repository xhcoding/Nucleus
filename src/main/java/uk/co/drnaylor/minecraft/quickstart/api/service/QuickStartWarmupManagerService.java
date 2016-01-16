package uk.co.drnaylor.minecraft.quickstart.api.service;

import org.spongepowered.api.scheduler.Task;

import java.util.UUID;

public interface QuickStartWarmupManagerService {
    void addWarmup(UUID player, Task task);

    boolean removeWarmup(UUID player);

    void cleanup();
}

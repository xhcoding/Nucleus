package uk.co.drnaylor.minecraft.quickstart.internal.tasks;

import org.spongepowered.api.scheduler.Task;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;

import java.util.function.Consumer;

public class CleanupTasks implements Consumer<Task> {

    private final QuickStart plugin;

    public CleanupTasks(QuickStart plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(Task task) {
        plugin.getUserLoader().purgeNotOnline();
    }
}

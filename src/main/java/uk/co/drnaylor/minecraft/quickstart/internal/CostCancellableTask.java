package uk.co.drnaylor.minecraft.quickstart.internal;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.CancellableTask;

public abstract class CostCancellableTask implements CancellableTask {

    private final double cost;
    private final QuickStart plugin;
    private final Player player;

    public CostCancellableTask(QuickStart plugin, Player src, double cost) {
        this.plugin = plugin;
        this.player = src;
        this.cost = cost;
    }

    @Override
    public void onCancel() {
        if (cost > 0) {
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> plugin.getEconHelper().depositInPlayer(player, cost));
        }
    }
}

package uk.co.drnaylor.minecraft.quickstart.listeners;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarmupManagerService;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;

public class WarmupListener extends ListenerBase {

    private QuickStartWarmupManagerService service = Sponge.getGame().getServiceManager().provideUnchecked(QuickStartWarmupManagerService.class);

    @Listener(order = Order.LAST)
    public void onPlayerMovement(DisplaceEntityEvent.Move event, @First Player player) {
        // Rotating is OK!
        if (event.getFromTransform().getLocation().equals(event.getToTransform().getLocation())) {
            cancelWarmup(player);
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerCommand(SendCommandEvent event, @First Player player) {
        cancelWarmup(player);
    }

    @Listener(order = Order.LAST)
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        cancelWarmup(event.getTargetEntity());
    }

    private void cancelWarmup(Player player) {
        service.cleanup();
        if (service.removeWarmup(player.getUniqueId()) && player.isOnline()) {
            player.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("warmup.cancel")));
        }
    }
}

package uk.co.drnaylor.minecraft.quickstart.listeners;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarmupManagerService;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;

public class WarmupListener extends ListenerBase {

    private QuickStartWarmupManagerService service = Sponge.getGame().getServiceManager().provideUnchecked(QuickStartWarmupManagerService.class);

    @Listener(order = Order.LAST)
    public void onPlayerMovement(DisplaceEntityEvent.Move event, @First Player player) {
        cancelWarmup(player);
    }

    @Listener(order = Order.LAST)
    public void onPlayerCommand(SendCommandEvent event, @First Player player) {
        cancelWarmup(player);
    }

    private void cancelWarmup(Player player) {
        service.cleanup();
        if (service.removeWarmup(player.getUniqueId())) {
            player.sendMessage(Text.of(TextColors.YELLOW, Util.messageBundle.getString("warmup.cancel")));
        }
    }
}

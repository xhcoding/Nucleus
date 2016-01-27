package uk.co.drnaylor.minecraft.quickstart.listeners;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;

import java.io.IOException;

@Modules(PluginModule.MISC)
public class MiscListener extends ListenerBase {

    // Do it first, so other plugins can have a say.
    @Listener(order = Order.FIRST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        try {
            if (plugin.getUserLoader().getUser(event.getTargetEntity()).isInvulnerable()) {
                event.getTargetEntity().offer(Keys.INVULNERABILITY, Integer.MAX_VALUE);
            } else {
                event.getTargetEntity().remove(Keys.INVULNERABILITY);
            }
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }
}

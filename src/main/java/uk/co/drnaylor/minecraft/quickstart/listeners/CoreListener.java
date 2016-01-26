package uk.co.drnaylor.minecraft.quickstart.listeners;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.time.Instant;

public class CoreListener extends ListenerBase {

    @Listener
    public void onPlayerJoin(final ClientConnectionEvent.Join event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            try {
                InternalQuickStartUser qsu = this.plugin.getUserLoader().getUser(event.getTargetEntity());
                qsu.setLastLogin(Instant.now());
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        });
    }

    @Listener
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            UserConfigLoader ucl = this.plugin.getUserLoader();
            try {
                InternalQuickStartUser qsu = null;
                qsu = this.plugin.getUserLoader().getUser(event.getTargetEntity());
                qsu.setLastLogout(Instant.now());
                ucl.purgeNotOnline();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        });
    }
}

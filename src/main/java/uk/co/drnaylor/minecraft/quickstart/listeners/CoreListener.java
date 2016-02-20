/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.listeners;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CoreListener extends ListenerBase {

    @Listener(order = Order.FIRST)
    public void onPlayerLogin(final ClientConnectionEvent.Login event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            try {
                InternalQuickStartUser qsu = this.plugin.getUserLoader().getUser(event.getTargetUser());
                qsu.setLastLogin(Instant.now());
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        });
    }

    @Listener
    public void onPlayerJoin(final ClientConnectionEvent.Join event) {
        try {
            // If we have a location to send them to in the config, send them there now!
            InternalQuickStartUser qsu = this.plugin.getUserLoader().getUser(event.getTargetEntity());
            Optional<Location<World>> olw = qsu.getLocationOnLogin();
            if (olw.isPresent()) {
                event.getTargetEntity().setLocation(olw.get());
                qsu.sendToLocationOnLogin(null);
            }
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }


    }

    @Listener
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
            UserConfigLoader ucl = this.plugin.getUserLoader();
            try {
                InternalQuickStartUser qsu = this.plugin.getUserLoader().getUser(event.getTargetEntity());
                qsu.setOnLogout();
                ucl.purgeNotOnline();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        }, 200, TimeUnit.MILLISECONDS);
    }
}

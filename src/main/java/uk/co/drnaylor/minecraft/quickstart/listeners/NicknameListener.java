/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.listeners;

import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.util.Optional;

@Modules(PluginModule.NICKNAME)
public class NicknameListener extends ListenerBase {

    @Inject private UserConfigLoader ucl;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player player) {
        InternalQuickStartUser iqsu;
        try {
            iqsu = ucl.getUser(player);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return;
        }

        Optional<Text> d = iqsu.getNicknameAsText();
        player.offer(Keys.SHOWS_DISPLAY_NAME, true);
        if (d.isPresent()) {
            player.offer(Keys.DISPLAY_NAME, d.get());
        } else {
            player.remove(Keys.DISPLAY_NAME);
        }
    }
}

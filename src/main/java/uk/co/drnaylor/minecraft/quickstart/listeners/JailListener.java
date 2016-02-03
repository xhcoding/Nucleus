package uk.co.drnaylor.minecraft.quickstart.listeners;

import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.services.JailHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Modules(PluginModule.JAILS)
public class JailListener extends ListenerBase {

    @Inject
    private UserConfigLoader loader;

    @Inject
    private JailHandler handler;

    @Inject
    private MainConfig config;

    /**
     * At the time the player joins, check to see if the player is muted.
     *
     * @param event The event.
     */
    @Listener
    public void onPlayerLogin(final ClientConnectionEvent.Join event) {
        // Kick off a scheduled task.
        Sponge.getScheduler().createTaskBuilder().async().delay(500, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    final Player user = event.getTargetEntity();
                    InternalQuickStartUser qs;
                    try {
                        qs = loader.getUser(user);
                    } catch (IOException | ObjectMappingException e) {
                        e.printStackTrace();
                        return;
                    }

                    Optional<JailData> omd = qs.getJailData();
                    if (omd.isPresent()) {
                        JailData md = omd.get();
                        md.nextLoginToTimestamp();

                        omd = Util.testForEndTimestamp(qs.getJailData(), () -> handler.unjailPlayer(user));
                        if (omd.isPresent()) {
                            md = omd.get();
                            onJail(md, event.getTargetEntity());
                        }
                    }
                }).submit(plugin);
    }

    @Listener
    public void onCommand(SendCommandEvent event, @Root Player player) {
        // Only if the command is not in the control list.
        if (checkJail(player) && config.getAllowedCommandsInJail().stream().anyMatch(x -> event.getCommand().toLowerCase().startsWith(x.toLowerCase()))) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        event.setCancelled(checkJail(player));
    }

    @Listener
    public void onInteract(InteractEvent event, @Root Player player) {
        event.setCancelled(checkJail(player));
    }

    private boolean checkJail(final Player player) {
        InternalQuickStartUser qs;
        try {
            qs = loader.getUser(player);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return false;
        }

        Optional<JailData> omd = Util.testForEndTimestamp(qs.getJailData(), () -> handler.unjailPlayer(player));
        if (omd.isPresent()) {
            onJail(omd.get(), player);
            return true;
        }

        return false;
    }

    private void onJail(JailData md, Player user) {
        if (md.getEndTimestamp().isPresent()) {
            user.sendMessage(Text.of(TextColors.RED, MessageFormat.format(
                    Util.messageBundle.getString("jail.playernotify.time"),
                    Util.getTimeStringFromSeconds(Instant.now().until(md.getEndTimestamp().get(), ChronoUnit.SECONDS)))));
        } else {
            user.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("jail.playernotify")));
        }
    }

}

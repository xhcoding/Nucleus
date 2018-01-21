/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.UniqueUserCountTransientModule;
import io.github.nucleuspowered.nucleus.modules.core.events.NucleusOnLoginEvent;
import io.github.nucleuspowered.nucleus.modules.core.events.OnFirstLoginEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;

import javax.annotation.Nullable;

public class CoreListener extends ListenerBase implements Reloadable {

    @Nullable private NucleusTextTemplate getKickOnStopMessage = null;

    @IsCancelled(Tristate.UNDEFINED)
    @Listener(order = Order.FIRST)
    public void onPlayerLoginFirst(final ClientConnectionEvent.Login event, @Getter("getTargetUser") User user) {
        // This works here. Not complaining.
        if (Util.isFirstPlay(user)) {
            Nucleus.getNucleus().getUserDataManager().get(user).ifPresent(qsu -> {
                CoreUserDataModule cu = qsu.get(CoreUserDataModule.class);
                if (!cu.getLastLogout().isPresent()) {
                    cu.setStartedFirstJoin(true);
                }
            });
        }
    }

    /* (non-Javadoc)
     * We do this last to avoid interfering with other modules.
     */
    @Listener(order = Order.LATE)
    public void onPlayerLoginLast(final ClientConnectionEvent.Login event, @Getter("getProfile") GameProfile profile,
        @Getter("getTargetUser") User user) {

        Nucleus.getNucleus().getUserDataManager().get(profile.getUniqueId()).ifPresent(qsu -> {
            if (event.getFromTransform().equals(event.getToTransform())) {
                CoreUserDataModule c = qsu.get(CoreUserDataModule.class);
                // Check this
                NucleusOnLoginEvent onLoginEvent =
                        CauseStackHelper.createFrameWithCausesWithReturn(cause ->
                                new NucleusOnLoginEvent(cause, user, qsu, event.getFromTransform()), profile);

                Sponge.getEventManager().post(onLoginEvent);
                if (onLoginEvent.getTo().isPresent()) {
                    event.setToTransform(onLoginEvent.getTo().get());
                    c.removeLocationOnLogin();
                    return;
                }

                // If we have a location to send them to in the config, send them there now!
                Optional<Location<World>> olw = c.getLocationOnLogin();
                olw.ifPresent(worldLocation -> {
                    event.setToTransform(event.getFromTransform().setLocation(worldLocation));
                    c.removeLocationOnLogin();
                });
            }

            plugin.getUserCacheService().updateCacheForPlayer(qsu);
        });
    }

    /* (non-Javadoc)
     * We do this first to try to get the first play status as quick as possible.
     */
    @Listener(order = Order.FIRST)
    public void onPlayerJoinFirst(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        try {
            ModularUserService qsu = Nucleus.getNucleus().getUserDataManager().getUnchecked(player);
            CoreUserDataModule c = qsu.get(CoreUserDataModule.class);
            c.setLastLogin(Instant.now());

            // If in the cache, unset it too.
            c.setFirstPlay(c.isStartedFirstJoin() && !c.getLastLogout().isPresent());

            if (c.isFirstPlay()) {
                plugin.getGeneralService().getTransient(UniqueUserCountTransientModule.class).resetUniqueUserCount();
            }

            c.setFirstJoin(player.getJoinData().firstPlayed().get());
            if (this.plugin.isServer()) {
                c.setLastIp(player.getConnection().getAddress().getAddress());
            }

            // We'll do this bit shortly - after the login events have resolved.
            final String name = player.getName();
            Task.builder().execute(() -> c.setLastKnownName(name)).delayTicks(20L).submit(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onPlayerJoinLast(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        if (Nucleus.getNucleus().getUserDataManager().get(player).map(x -> x.get(CoreUserDataModule.class).isFirstPlay()).orElse(true)) {
            NucleusFirstJoinEvent firstJoinEvent = new OnFirstLoginEvent(
                event.getCause(), player, event.getOriginalChannel(), event.getChannel().orElse(null), event.getOriginalMessage(),
                    event.isMessageCancelled(), event.getFormatter());

            Sponge.getEventManager().post(firstJoinEvent);
            event.setChannel(firstJoinEvent.getChannel().get());
            event.setMessageCancelled(firstJoinEvent.isMessageCancelled());
            Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(CoreUserDataModule.class).setStartedFirstJoin(false);
        }
    }

    @Listener(beforeModifications = true)
    @SuppressWarnings("ConstantConditionalExpression")
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        // There is an issue in Sponge where the connection may not even exist, because they were disconnected before the connection was
        // completely established.
        //noinspection ConstantConditions
        if (player.getConnection() == null || player.getConnection().getAddress() == null) {
            return;
        }

        this.plugin.getUserDataManager().get(player).ifPresent(x -> onPlayerQuit(x, player));
    }

    private void onPlayerQuit(ModularUserService x, Player player) {
        final Location<World> location = player.getLocation();
        final InetAddress address = player.getConnection().getAddress().getAddress();

        try {
            CoreUserDataModule coreUserDataModule = x.get(CoreUserDataModule.class);
            coreUserDataModule.setLastIp(address);
            coreUserDataModule.setLastLogout(location);
            x.save();
            plugin.getUserCacheService().updateCacheForPlayer(x);
        } catch (Exception e) {
            Nucleus.getNucleus().printStackTraceIfDebugMode(e);
        }
    }

    @Override public void onReload() throws Exception {
        CoreConfig c = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CoreConfigAdapter.class)
                .getNodeOrDefault();
        this.getKickOnStopMessage = c.isKickOnStop() ? c.getKickOnStopMessage() : null;
    }

    @Listener
    public void onServerAboutToStop(final GameStoppingServerEvent event) {
        plugin.getUserDataManager().getOnlineUsers().forEach(x -> x.getPlayer().ifPresent(y -> onPlayerQuit(x, y)));

        if (this.getKickOnStopMessage != null) {
            Iterator<Player> players = Sponge.getServer().getOnlinePlayers().iterator();
            while (players.hasNext()) {
                Player p = players.next();
                Text msg = this.getKickOnStopMessage.getForCommandSource(p);
                if (msg.isEmpty()) {
                    p.kick();
                } else {
                    p.kick(msg);
                }
            }
        }

    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        CommandSource requester = event.getCause().first(CommandSource.class).orElse(Sponge.getServer().getConsole());
        if (plugin.reload()) {
            requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ", plugin.getMessageProvider().getTextMessageWithFormat("command.reload.one")));
            requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ", plugin.getMessageProvider().getTextMessageWithFormat("command.reload.two")));
        } else {
            requester.sendMessage(Text.of(TextColors.RED, "[Nucleus] ", plugin.getMessageProvider().getTextMessageWithFormat("command.reload.errorone")));
        }
    }
}

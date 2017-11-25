/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.listeners;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.events.NucleusMessageEvent;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.message.events.InternalNucleusHelpOpEvent;
import io.github.nucleuspowered.nucleus.modules.mute.commands.MuteCommand;
import io.github.nucleuspowered.nucleus.modules.mute.commands.VoiceCommand;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfig;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MuteListener extends ListenerBase implements Reloadable {

    private final MuteHandler handler = getServiceUnchecked(MuteHandler.class);
    private MuteConfig muteConfig = new MuteConfig();
    private final String voicePerm = getPermissionHandlerFor(VoiceCommand.class).getPermissionWithSuffix("auto");

    /**
     * At the time the subject joins, check to see if the subject is muted.
     *
     * @param event The event.
     */
    @Listener
    public void onPlayerLogin(final ClientConnectionEvent.Join event) {
        // Kick off a scheduled task.
        Sponge.getScheduler().createTaskBuilder().async().delay(500, TimeUnit.MILLISECONDS).execute(() -> {
            Player user = event.getTargetEntity();
            Optional<MuteData> omd = handler.getPlayerMuteData(user);
            if (omd.isPresent()) {
                MuteData md = omd.get();
                md.nextLoginToTimestamp();

                omd = Util.testForEndTimestamp(handler.getPlayerMuteData(user), () -> handler.unmutePlayer(user));
                if (omd.isPresent()) {
                    md = omd.get();
                    this.handler.onMute(md, event.getTargetEntity());
                }
            }
        }).submit(plugin);
    }

    @Listener(order = Order.LATE)
    public void onChat(MessageChannelEvent.Chat event) {
        Util.onPlayerSimulatedOrPlayer(event, this::onChat);
    }

    private void onChat(MessageChannelEvent.Chat event, Player player) {
        boolean cancel = false;
        Optional<MuteData> omd = Util.testForEndTimestamp(handler.getPlayerMuteData(player), () -> handler.unmutePlayer(player));
        if (omd.isPresent()) {
            this.handler.onMute(omd.get(), player);
            MessageChannel.TO_CONSOLE.send(Text.builder().append(Text.of(player.getName() + " (")).append(plugin.getMessageProvider().getTextMessageWithFormat("standard.muted"))
                    .append(Text.of("): ")).append(event.getRawMessage()).build());
            cancel = true;
        }

        if (cancelOnGlobalMute(player, false)) {
            cancel = true;
        }

        if (cancel) {
            if (this.muteConfig.isShowMutedChat()) {
                // Send it to admins only.
                String m = this.muteConfig.getCancelledTag();
                if (!m.isEmpty()) {
                    event.getFormatter().setHeader(
                        Text.join(TextSerializers.FORMATTING_CODE.deserialize(m), event.getFormatter().getHeader().toText()));
                }

                new PermissionMessageChannel(MuteCommand.getMutedChatPermission()).send(player, event.getMessage(), ChatTypes.SYSTEM);
            }

            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerMessage(NucleusMessageEvent event) {
        if (!(event.getSender() instanceof Player)) {
            return;
        }

        boolean isCancelled = false;
        Player user = (Player)event.getSender();
        Optional<MuteData> omd = Util.testForEndTimestamp(handler.getPlayerMuteData(user), () -> handler.unmutePlayer(user));
        if (omd.isPresent()) {
            if (user.isOnline()) {
                this.handler.onMute(omd.get(), user.getPlayer().get());
            }

            isCancelled = true;
        }

        if (cancelOnGlobalMute(user, isCancelled)) {
            isCancelled = true;
        }

        event.setCancelled(isCancelled);
    }

    @Listener
    public void onPlayerHelpOp(InternalNucleusHelpOpEvent event, @Root Player user) {
        Optional<MuteData> omd = Util.testForEndTimestamp(handler.getPlayerMuteData(user), () -> handler.unmutePlayer(user));
        omd.ifPresent(muteData -> {
            if (user.isOnline()) {
                this.handler.onMute(muteData, user.getPlayer().get());
            }

            event.setCancelled(true);
        });

        if (cancelOnGlobalMute(user, event.isCancelled())) {
            event.setCancelled(true);
        }
    }

    private boolean cancelOnGlobalMute(Player player, boolean isCancelled) {
        if (isCancelled || !handler.isGlobalMuteEnabled() || player.hasPermission(this.voicePerm)) {
            return false;
        }

        if (handler.isVoiced(player.getUniqueId())) {
            return false;
        }

        player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("globalmute.novoice"));
        return true;
    }

    @Override
    public void onReload() throws Exception {
        this.muteConfig = getServiceUnchecked(MuteConfigAdapter.class).getNodeOrDefault();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ignore.listeners;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.chat.NucleusNoIgnoreChannel;
import io.github.nucleuspowered.nucleus.api.events.NucleusMailEvent;
import io.github.nucleuspowered.nucleus.api.events.NucleusMessageEvent;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.ignore.commands.IgnoreCommand;
import io.github.nucleuspowered.nucleus.modules.ignore.datamodules.IgnoreUserDataModule;
import io.github.nucleuspowered.nucleus.util.MessageChannelWrapper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class IgnoreListener extends ListenerBase {

    @Inject private PermissionRegistry permissionRegistry;
    @Inject private UserDataManager loader;
    @Inject private CoreConfigAdapter cca;
    private CommandPermissionHandler ignoreHandler;

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        if (event.getChannel().orElseGet(event::getOriginalChannel) instanceof NucleusNoIgnoreChannel) {
            return;
        }

        // Reset the channel - but only if we have to.
        checkCancels(event.getChannel().orElseGet(event::getOriginalChannel).getMembers(), player).ifPresent(x ->
                event.setChannel(new MessageChannelWrapper(event.getChannel().orElseGet(event::getOriginalChannel), x)));
    }

    @Listener(order = Order.FIRST)
    public void onMessage(NucleusMessageEvent event, @Root Player player) {
        if (event.getRecipient() instanceof User) {
            try {
                event.setCancelled(loader.get((User) event.getRecipient()).get()
                        .get(IgnoreUserDataModule.class)
                        .getIgnoreList().contains(player.getUniqueId()));
            } catch (Exception e) {
                if (cca.getNodeOrDefault().isDebugmode()) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Listener(order = Order.FIRST)
    public void onMail(NucleusMailEvent event, @Root Player player) {
        try {
            event.setCancelled(loader.get(event.getRecipient()).get()
                    .get(IgnoreUserDataModule.class)
                    .getIgnoreList().contains(player.getUniqueId()));
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if we need to cancel messages to people.
     *
     * @param collection The collection to check through.
     * @param player The subject who is sending the message.
     * @return {@link Optional} if unchanged, otherwise a {@link Collection} of {@link MessageReceiver}s
     */
    private Optional<Collection<MessageReceiver>> checkCancels(Collection<MessageReceiver> collection, Player player) {
        if (ignoreHandler == null) {
            ignoreHandler = permissionRegistry.getPermissionsForNucleusCommand(IgnoreCommand.class);
        }

        if (ignoreHandler.testSuffix(player, "exempt.chat")) {
            return Optional.empty();
        }

        List<MessageReceiver> list = Lists.newArrayList(collection);
        list.removeIf(x -> {
            try {
                return x instanceof Player && !x.equals(player) && loader.get((Player) x).get()
                        .get(IgnoreUserDataModule.class)
                        .getIgnoreList().contains(player.getUniqueId());
            } catch (Exception e) {
                if (cca.getNodeOrDefault().isDebugmode()) {
                    e.printStackTrace();
                }

                return false;
            }
        });

        // We do this so we don't have to recreate a channel if nothing changes.
        if (list.size() == collection.size()) {
            return Optional.empty();
        }

        return Optional.of(list);
    }
}

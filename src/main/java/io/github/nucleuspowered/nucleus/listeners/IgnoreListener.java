/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.listeners;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.events.MailEvent;
import io.github.nucleuspowered.nucleus.api.events.MessageEvent;
import io.github.nucleuspowered.nucleus.commands.ignore.IgnoreCommand;
import io.github.nucleuspowered.nucleus.config.MainConfig;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Modules(PluginModule.IGNORE)
public class IgnoreListener extends ListenerBase {

    @Inject private PermissionRegistry permissionRegistry;
    @Inject private UserConfigLoader loader;
    @Inject private MainConfig config;
    private CommandPermissionHandler ignoreHandler;

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event, @First Player player) {
        // Reset the channel - but only if we have to.
        checkCancels(event.getChannel().orElse(event.getOriginalChannel()).getMembers(), player).ifPresent(x -> event.setChannel(MessageChannel.fixed(x)));
    }

    @Listener(order = Order.FIRST)
    public void onMessage(MessageEvent event, @First Player player) {
        if (event.getRecipient() instanceof User) {
            try {
                event.setCancelled(loader.getUser((User) event.getRecipient()).getIgnoreList().contains(player.getUniqueId()));
            } catch (IOException | ObjectMappingException e) {
                if (config.getDebugMode()) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Listener(order = Order.FIRST)
    public void onMail(MailEvent event, @First Player player) {
        try {
            event.setCancelled(loader.getUser(event.getRecipient()).getIgnoreList().contains(player.getUniqueId()));
        } catch (IOException | ObjectMappingException e) {
            if (config.getDebugMode()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if we need to cancel messages to people.
     *
     * @param collection The collection to check through.
     * @param player The player who is sending the message.
     * @return {@link Optional} if unchanged, otherwise a {@link Collection} of {@link MessageReceiver}s
     */
    private Optional<Collection<MessageReceiver>> checkCancels(Collection<MessageReceiver> collection, Player player) {
        if (ignoreHandler == null) {
            permissionRegistry.getService(IgnoreCommand.class).ifPresent(x -> ignoreHandler = x);
            if (ignoreHandler == null) {
                return Optional.empty();
            }
        }

        if (ignoreHandler.testSuffix(player, "exempt.chat")) {
            return Optional.empty();
        }

        List<MessageReceiver> list = Lists.newArrayList(collection);
        list.removeIf(x -> {
            try {
                return x instanceof Player && !x.equals(player) && loader.getUser((Player) x).getIgnoreList().contains(player.getUniqueId());
            } catch (IOException | ObjectMappingException e) {
                if (config.getDebugMode()) {
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

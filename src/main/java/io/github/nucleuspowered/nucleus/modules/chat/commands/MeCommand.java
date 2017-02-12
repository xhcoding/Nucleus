/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.commands;

import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chat.listeners.ChatListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@SuppressWarnings("ALL")
@RegisterCommand({"me", "action"})
@Permissions(suggestedLevel = SuggestedLevel.USER)
public class MeCommand extends AbstractCommand<CommandSource> {

    @Inject private ChatConfigAdapter chatConfigAdapter;
    private final MeChannel channel = new MeChannel();

    private final String messageKey = "message";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            new RemainingStringsArgument(Text.of(messageKey))
        };
    }

    @Override
    public CommandResult executeCommand(@Nonnull CommandSource src, CommandContext args) throws Exception {
        String message = ChatListener.stripPermissionless(src, args.<String>getOne(messageKey).get());

        Text header = plugin.getChatUtil()
                .getMessageFromTemplate(chatConfigAdapter.getNodeOrDefault().getMePrefix(), src, false);
        ChatUtil.StyleTuple t = plugin.getChatUtil().getLastColourAndStyle(header, null);
        MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter(
            Text.builder().color(t.colour).style(t.style)
                .append(TextSerializers.FORMATTING_CODE.deserialize(message)).toText()
        );

        // Doing this here rather than in the constructor removes the < > notation.
        formatter.setHeader(header);

        // We create an event so that other plugins can provide transforms, such as Boop, and that we
        // can catch it in ignore and mutes, and so can other plugins.
        MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
            Cause.source(src).build(),
            channel,
            Optional.of(channel),
            formatter,
            Text.of(message),
            false
        );

        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.me.cancel"));
        }

        event.getChannel().orElse(channel).send(src, Util.applyChatTemplate(event.getFormatter()), ChatTypes.CHAT);
        return CommandResult.success();
    }

    public class MeChannel implements NucleusChatChannel.ActionMessage {

        @Override
        @Nonnull
        public Collection<MessageReceiver> getMembers() {
            return MessageChannel.TO_ALL.getMembers();
        }

        @Override public boolean removePrefix() {
            return false;
        }
    }
}

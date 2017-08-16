/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.event.NucleusMessageChannelEvent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chat.listeners.ChatListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@SuppressWarnings("ALL")
@RegisterCommand({"me", "action"})
@Permissions(suggestedLevel = SuggestedLevel.USER)
@EssentialsEquivalent({"me", "action", "describe"})
public class MeCommand extends AbstractCommand<CommandSource> implements AbstractCommand.Reloadable {

    @Inject private ChatConfigAdapter chatConfigAdapter;
    private ChatConfig config = null;
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
        Text header = config.getMePrefix().getForCommandSource(src);
        TextParsingUtils.StyleTuple t = plugin.getTextParsingUtils().getLastColourAndStyle(header, null);
        Text originalMessage = TextSerializers.FORMATTING_CODE.deserialize(message);
        MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter(
            Text.builder().color(t.colour).style(t.style)
                .append(TextSerializers.FORMATTING_CODE.deserialize(message)).toText()
        );

        // Doing this here rather than in the constructor removes the < > notation.
        formatter.setHeader(header);

        // We create an event so that other plugins can provide transforms, such as Boop, and that we
        // can catch it in ignore and mutes, and so can other plugins.
        MessageChannelEvent.Chat event = new NucleusMessageChannelEvent(
                Cause.source(src).build(),
                channel,
                originalMessage,
                formatter);

        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.me.cancel"));
        }

        event.getChannel().orElse(channel).send(src, Util.applyChatTemplate(event.getFormatter()), ChatTypes.CHAT);
        return CommandResult.success();
    }

    @Override public void onReload() {
        config = chatConfigAdapter.getNodeOrDefault();
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

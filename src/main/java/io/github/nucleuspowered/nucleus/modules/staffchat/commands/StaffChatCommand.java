/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.event.NucleusMessageChannelEvent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import io.github.nucleuspowered.nucleus.modules.staffchat.datamodules.StaffChatTransientModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"staffchat", "sc", "a"})
public class StaffChatCommand extends AbstractCommand<CommandSource> {

    private final String message = "message";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(new RemainingStringsArgument(Text.of(message)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<String> toSend = args.getOne(message);
        StaffChatMessageChannel scmc = StaffChatMessageChannel.getInstance();
        if (toSend.isPresent()) {
            Text rawMessage = TextSerializers.FORMATTING_CODE.deserialize(toSend.get());
            if (src instanceof Player) {
                Player pl = (Player)src;
                MessageChannelEvent.Chat event = new NucleusMessageChannelEvent(
                    Cause.source(pl).named(NamedCause.notifier(src)).build(),
                    scmc,
                    rawMessage,
                    new MessageEvent.MessageFormatter(Text.builder(pl.getName())
                        .onShiftClick(TextActions.insertText(pl.getName()))
                        .onClick(TextActions.suggestCommand("/msg " + pl.getName()))
                        .build(), rawMessage));

                if (!Sponge.getEventManager().post(event)) {
                    scmc.send(pl, Util.applyChatTemplate(event.getFormatter()));
                    return CommandResult.success();
                }

                throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.staffchat.cancelled"));
            }

            scmc.send(src, TextSerializers.FORMATTING_CODE.deserialize(toSend.get()));
            return CommandResult.success();
        }

        if (!(src instanceof Player)) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.staffchat.consoletoggle"));
        }

        Player player = (Player)src;

        StaffChatTransientModule s = plugin.getUserDataManager().getUnchecked(player).getTransient(StaffChatTransientModule.class);

        boolean result = !(src.getMessageChannel() instanceof StaffChatMessageChannel);
        if (result) {
            s.setPreviousMessageChannel(player.getMessageChannel());
            src.setMessageChannel(StaffChatMessageChannel.getInstance());
        } else {
            src.setMessageChannel(s.getPreviousMessageChannel().orElse(MessageChannel.TO_ALL));
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.staffchat." + (result ? "on" : "off")));
        return CommandResult.success();
    }
}

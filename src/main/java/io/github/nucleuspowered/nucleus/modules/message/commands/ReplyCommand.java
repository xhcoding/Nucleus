/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.internal.annotations.ConfigCommandAlias;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

/**
 * Replies to the last player who sent a message.
 *
 * Permission: quickstart.message.base
 */
@Permissions(alias = "message", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@ConfigCommandAlias(value = "message", generate = false)
@RegisterCommand({"reply", "r"})
public class ReplyCommand extends CommandBase<CommandSource> {

    private final String message = "message";

    @Inject private MessageHandler handler;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean b = handler.replyMessage(src, args.<String>getOne(message).get());
        return b ? CommandResult.success() : CommandResult.empty();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.PlayerConsoleArgument;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Messages a player.
 *
 * Permission: quickstart.message.base
 */
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand({ "message", "m", "msg", "whisper", "w", "tell", "t" })
public class MessageCommand extends CommandBase<CommandSource> {
    private final String to = "to";
    private final String message = "message";

    @Inject private MessageHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
            .arguments(
                new PlayerConsoleArgument(Text.of(to)),
                GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))
            ).description(Text.of("Send a message to a player")).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean b = handler.sendMessage(src, args.<CommandSource>getOne(to).get(), args.<String>getOne(message).get());
        return b ? CommandResult.success() : CommandResult.empty();
    }
}

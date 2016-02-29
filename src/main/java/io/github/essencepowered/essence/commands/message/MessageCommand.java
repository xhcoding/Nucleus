/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.message;

import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.argumentparsers.PlayerConsoleArgument;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.annotations.RunAsync;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
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
@Modules(PluginModule.MESSAGES)
@RunAsync
@RegisterCommand({ "message", "m", "msg", "whisper", "w", "tell", "t" })
public class MessageCommand extends CommandBase<CommandSource> {
    private final String to = "to";
    private final String message = "message";

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
        boolean b = plugin.getMessageHandler().sendMessage(src, args.<CommandSource>getOne(to).get(), args.<String>getOne(message).get());
        return b ? CommandResult.success() : CommandResult.empty();
    }
}

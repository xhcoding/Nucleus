/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.message;

import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Replies to the last player who sent a message.
 *
 * Permission: quickstart.message.base
 */
@Permissions(alias = "message", suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.MESSAGES)
@RunAsync
@ConfigCommandAlias(value = "message", generate = false)
@RegisterCommand({"reply", "r"})
public class ReplyCommand extends CommandBase<CommandSource> {
    private final String message = "message";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))
            ).description(Text.of("Send a message to the player you just sent a message to.")).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean b = plugin.getMessageHandler().replyMessage(src, args.<String>getOne(message).get());
        return b ? CommandResult.success() : CommandResult.empty();
    }
}

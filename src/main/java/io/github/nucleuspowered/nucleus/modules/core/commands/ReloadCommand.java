/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;

@Permissions(root = "nucleus")
@NoCooldown
@NoCost
@NoWarmup
@RunAsync
@RegisterCommand(value = "reload", subcommandOf = NucleusCommand.class)
public class ReloadCommand extends CommandBase<CommandSource> {

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        plugin.reload();
        src.sendMessage(Util.getTextMessageWithFormat("command.reload.one"));
        src.sendMessage(Util.getTextMessageWithFormat("command.reload.two"));
        return CommandResult.success();
    }
}

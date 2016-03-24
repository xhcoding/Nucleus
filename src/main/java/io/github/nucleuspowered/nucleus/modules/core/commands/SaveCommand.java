/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;

/**
 * Saves the data files.
 *
 * Permission: nucleus.nucleus.save
 */
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@Permissions(root = "nucleus")
@RegisterCommand(value = "save", subcommandOf = NucleusCommand.class)
public class SaveCommand extends CommandBase<CommandSource> {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        src.sendMessage(Util.getTextMessageWithFormat("command.nucleus.save.start"));
        plugin.saveData();
        return CommandResult.success();
    }
}

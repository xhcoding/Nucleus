/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

import javax.inject.Inject;

@Permissions
@NoCost
@NoCooldown
@NoWarmup
@RunAsync
@RegisterCommand(value = { "clear", "remove" }, subcommandOf = FirstKitCommand.class)
public class FirstKitClearCommand extends CommandBase<CommandSource> {

    @Inject
    private GeneralDataStore gds;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        gds.setFirstKit(null);
        src.sendMessage(Util.getTextMessageWithFormat("command.firstkit.clear.success"));
        return CommandResult.success();
    }
}

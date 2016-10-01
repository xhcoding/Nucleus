/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

import javax.inject.Inject;

@Permissions(prefix = "firstjoinkit")
@NoCost
@NoCooldown
@NoWarmup
@RunAsync
@RegisterCommand(value = { "clear", "remove" }, subcommandOf = FirstKitCommand.class)
public class FirstKitClearCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    @Inject
    private GeneralService gds;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        gds.setFirstKit(null);
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.clear.success"));
        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;

/**
 * nucleus.firstspawn.remove.base
 */
@RegisterCommand(value = {"del", "rm"}, subcommandOf = SetFirstSpawnCommand.class)
@Permissions(root = "firstspawn", alias = "remove")
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
public class RemoveFirstSpawnCommand extends OldCommandBase<CommandSource> {

    @Inject private GeneralDataStore data;

    @Override
    public CommandSpec createSpec() {
        return getSpecBuilderBase().build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        data.removeFirstSpawn();
        src.sendMessage(Util.getTextMessageWithFormat("command.setfirstspawn.remove"));
        return CommandResult.success();
    }
}

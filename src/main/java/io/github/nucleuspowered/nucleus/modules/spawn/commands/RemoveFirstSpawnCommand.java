/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnGeneralDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

/**
 * plugin.firstspawn.remove.base
 */
@RegisterCommand(value = {"del", "rm"}, subcommandOf = SetFirstSpawnCommand.class)
@Permissions(prefix = "firstspawn", mainOverride = "remove")
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
public class RemoveFirstSpawnCommand extends AbstractCommand<CommandSource> {

    @Inject private ModularGeneralService data;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        data.get(SpawnGeneralDataModule.class).removeFirstSpawn();
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.setfirstspawn.remove"));
        return CommandResult.success();
    }
}

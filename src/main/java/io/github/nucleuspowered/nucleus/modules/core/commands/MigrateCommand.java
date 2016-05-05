/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.migrators.EssCmdsMigrator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

/**
 * Saves the data files.
 *
 * Permission: nucleus.nucleus.migrate
 */
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@Permissions(root = "nucleus")
@RegisterCommand(value = "migrate", subcommandOf = NucleusCommand.class)
public class MigrateCommand extends CommandBase<CommandSource> {

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        if (Sponge.getPluginManager().getPlugin("io.github.hsyyid.essentialcmds").isPresent()) {
            try {
                plugin.getInjector().getInstance(EssCmdsMigrator.class).migrate(src);
                return CommandResult.success();
            } catch (Exception e) {
                e.printStackTrace();
                src.sendMessage(Util.getTextMessageWithFormat("command.nucleus.migrate.error"));
            }
        } else {
            src.sendMessage(Util.getTextMessageWithFormat("command.nucleus.migrate.noessentialcmds"));
        }

        return CommandResult.empty();
    }
}

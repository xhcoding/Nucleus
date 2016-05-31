/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

import javax.inject.Inject;

/**
 * Clears the {@link UserConfigLoader} cache, so any offline user's files wll be read on next startup.
 */
@Permissions(root = "nucleus")
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand(value = "clearcache", subcommandOf = NucleusCommand.class)
public class ClearCacheCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader ucl;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        ucl.clearSoftReferenceCache();
        src.sendMessage(Util.getTextMessageWithFormat("command.nucleus.clearcache.success"));
        return CommandResult.success();
    }
}

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
                // I imagine that there may be a few questions that you may be asking here.
                //
                // 1) Why is there an injector here?
                // 2) Why not just bung the logic in this class?
                //
                // Using an injector allows us to put all our handlers into the class easily, much like with a command.
                // The code in the migrator existed in this command originally anyway, so using an injector made it easy
                // to move the code.
                //
                // Of course, that brings us to why the logic is not in this class anyway - again, due to the injector!
                // We use Guice to inject members into all our command classes, it's part of the magic that allows us
                // to have a module system. EssentialCmds uses a static class to access configuration, and the injector
                // will try to load the class. Normally, this is fine... until the class does not exist - such as when
                // EssentialCmds is NOT installed! We then get an error on the console, and confuse the user. So, moving
                // that logic into a class that loads on demand means that the nasty error no longer occurs.
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

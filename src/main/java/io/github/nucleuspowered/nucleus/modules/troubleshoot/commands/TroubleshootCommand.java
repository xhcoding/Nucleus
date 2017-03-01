/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.troubleshoot.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

@Permissions
@RegisterCommand(value = "troubleshoot", hasExecutor = false)
public class TroubleshootCommand extends AbstractCommand<CommandSource> {

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        return null;
    }
}

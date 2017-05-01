/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.firstjoin;

import io.github.nucleuspowered.nucleus.argumentparsers.AlternativeUsageArgument;
import io.github.nucleuspowered.nucleus.dataservices.KitService;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@Permissions
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
@RegisterCommand(value = {"firstjoinkit", "starterkit", "joinkit", "firstkit"}, hasExecutor = false)
public class FirstKitCommand extends AbstractCommand<CommandSource> {

    @Inject private KitService gds;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
                new AlternativeUsageArgument(GenericArguments.remainingRawJoinedStrings(Text.NEW_LINE),
                        c -> Text.EMPTY)
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        return CommandResult.success();
    }
}

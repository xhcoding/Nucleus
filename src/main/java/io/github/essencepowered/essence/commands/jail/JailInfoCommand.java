/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.jail;

import com.google.inject.Inject;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.data.WarpLocation;
import io.github.essencepowered.essence.argumentparsers.JailParser;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.NoCooldown;
import io.github.essencepowered.essence.internal.annotations.NoCost;
import io.github.essencepowered.essence.internal.annotations.NoWarmup;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.annotations.RunAsync;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.JailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@NoCooldown
@NoCost
@NoWarmup
@Permissions(root = "jail", suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@RegisterCommand(value = "jails", subcommandOf = JailsCommand.class)
public class JailInfoCommand extends CommandBase<CommandSource> {

    private final String jailKey = "jail";
    @Inject private JailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new JailParser(Text.of(jailKey), handler))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WarpLocation wl = args.<WarpLocation>getOne(jailKey).get();
        src.sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.jail.info.name"))
                .append(Text.of(": ", TextColors.GREEN, wl.getName())).build());
        src.sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.jail.info.location"))
                .append(Text.of(": ", TextColors.GREEN, wl.toLocationString())).build());
        return CommandResult.success();
    }
}

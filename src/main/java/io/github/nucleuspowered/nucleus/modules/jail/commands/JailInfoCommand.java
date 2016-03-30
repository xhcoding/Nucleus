/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.argumentparsers.JailParser;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
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
public class JailInfoCommand extends OldCommandBase<CommandSource> {

    private final String jailKey = "jail";
    @Inject private JailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return getSpecBuilderBase().arguments(GenericArguments.onlyOne(new JailParser(Text.of(jailKey), handler))).build();
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

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
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

@Permissions(root = "jail")
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand(value = {"delete", "del", "remove"}, subcommandOf = JailsCommand.class)
public class DeleteJailCommand extends OldCommandBase<CommandSource> {

    @Inject private JailHandler handler;
    private final String jailKey = "jail";

    @Override
    public CommandSpec createSpec() {
        return getSpecBuilderBase().arguments(GenericArguments.onlyOne(new JailParser(Text.of(jailKey), handler))).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WarpLocation wl = args.<WarpLocation>getOne(jailKey).get();
        if (handler.removeJail(wl.getName())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.jails.del.success", wl.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.jails.del.error", wl.getName()));
        return CommandResult.empty();
    }
}

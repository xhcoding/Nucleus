/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.home;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.argumentparsers.HomeOtherParser;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Permissions(root = "home", alias = "deleteother")
@Modules(PluginModule.HOMES)
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"deletehomeother", "delhomeother"})
public class DeleteOtherHomeCommand extends CommandBase<CommandSource> {

    private final String homeKey = "home";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new HomeOtherParser(Text.of(homeKey), plugin)))
                .executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        HomeOtherParser.HomeData wl = args.<HomeOtherParser.HomeData>getOne(homeKey).get();
        if (plugin.getUserLoader().getUser(wl.user).deleteHome(wl.location.getName())) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.home.delete.other.success", wl.user.getName(), wl.location.getName())));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.home.delete.other.fail", wl.user.getName(), wl.location.getName())));
        return CommandResult.empty();
    }
}

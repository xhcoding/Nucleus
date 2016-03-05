/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.home;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeOtherParser;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

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
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new HomeOtherParser(Text.of(homeKey), plugin))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        HomeOtherParser.HomeData wl = args.<HomeOtherParser.HomeData>getOne(homeKey).get();
        if (plugin.getUserLoader().getUser(wl.user).deleteHome(wl.location.getName())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.home.delete.other.success", wl.user.getName(), wl.location.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.home.delete.other.fail", wl.user.getName(), wl.location.getName()));
        return CommandResult.empty();
    }
}

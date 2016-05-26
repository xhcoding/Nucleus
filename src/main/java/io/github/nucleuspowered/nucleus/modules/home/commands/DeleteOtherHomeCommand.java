/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeOtherArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

@Permissions(root = "home", alias = "deleteother")
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"deletehomeother", "delhomeother"})
public class DeleteOtherHomeCommand extends CommandBase<CommandSource> {

    private final String homeKey = "home";

    @Inject private CoreConfigAdapter cca;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new HomeOtherArgument(Text.of(homeKey), plugin, cca))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        HomeOtherArgument.HomeData wl = args.<HomeOtherArgument.HomeData>getOne(homeKey).get();
        if (plugin.getUserLoader().getUser(wl.user).deleteHome(wl.location.getName())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.home.delete.other.success", wl.user.getName(), wl.location.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.home.delete.other.fail", wl.user.getName(), wl.location.getName()));
        return CommandResult.empty();
    }
}

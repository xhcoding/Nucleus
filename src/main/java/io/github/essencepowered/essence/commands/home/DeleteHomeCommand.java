/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.home;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.api.data.WarpLocation;
import io.github.essencepowered.essence.argumentparsers.HomeParser;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Permissions(alias = "home", suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.HOMES)
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"deletehome", "delhome"})
public class DeleteHomeCommand extends CommandBase<Player> {

    private final String homeKey = "home";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new HomeParser(Text.of(homeKey), plugin)))
                .executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        WarpLocation wl = args.<WarpLocation>getOne(homeKey).get();
        if (plugin.getUserLoader().getUser(src).deleteHome(wl.getName())) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.home.delete.success", wl.getName())));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.home.delete.fail", wl.getName())));
        return CommandResult.empty();
    }
}

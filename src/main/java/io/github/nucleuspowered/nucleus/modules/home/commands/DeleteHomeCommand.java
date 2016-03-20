/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeParser;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@Permissions(alias = "home", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"deletehome", "delhome"})
public class DeleteHomeCommand extends CommandBase<Player> {

    private final String homeKey = "home";

    @Inject private CoreConfigAdapter cca;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new HomeParser(Text.of(homeKey), plugin, cca))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        WarpLocation wl = args.<WarpLocation>getOne(homeKey).get();
        if (plugin.getUserLoader().getUser(src).deleteHome(wl.getName())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.home.delete.success", wl.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.home.delete.fail", wl.getName()));
        return CommandResult.empty();
    }
}

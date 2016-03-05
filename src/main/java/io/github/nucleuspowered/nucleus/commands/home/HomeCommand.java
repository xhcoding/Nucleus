/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.home;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeParser;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.HOMES)
@RegisterCommand("home")
public class HomeCommand extends CommandBase<Player> {

    private final String home = "home";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .arguments(GenericArguments.onlyOne(GenericArguments.optional(new HomeParser(Text.of(home), plugin)))).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        Optional<WarpLocation> owl = args.<WarpLocation>getOne(home);
        if (!owl.isPresent()) {
            owl = plugin.getUserLoader().getUser(src).getHome("home");

            if (!owl.isPresent()) {
                src.sendMessage(Util.getTextMessageWithFormat("args.home.nohome", "home"));
                return CommandResult.empty();
            }
        }

        WarpLocation wl = owl.get();

        // Warp to it safely.
        if (src.setLocationAndRotationSafely(wl.getLocation(), wl.getRotation())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.home.success", wl.getName()));
            return CommandResult.success();
        } else {
            src.sendMessage(Util.getTextMessageWithFormat("command.home.fail", wl.getName()));
            return CommandResult.empty();
        }
    }
}

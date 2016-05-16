/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedGameModeParser;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

/**
 * Sets gamemode of world.
 *
 * Command Usage: /world setgamemode [gamemode] [world] Permission:
 * nucleus.world.setgamemode.base
 */
@Permissions(root = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"setgamemode", "setgm", "gamemode", "gm"}, subcommandOf = WorldCommand.class)
public class SetGamemodeWorldCommand extends CommandBase<CommandSource> {

    private final String gamemode = "gamemode";
    private final String world = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new ImprovedGameModeParser(Text.of(gamemode))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.world(Text.of(world))))};
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        GameMode gamemodeInput = args.<GameMode>getOne(gamemode).get();
        Optional<WorldProperties> optWorldProperties = args.getOne(world);

        if (optWorldProperties.isPresent()) {
            optWorldProperties.get().setGameMode(gamemodeInput);
            src.sendMessage(Util.getTextMessageWithFormat("command.world.setgamemode.success"));
        } else {
            if (src instanceof Player) {
                Player player = (Player) src;
                player.getWorld().getProperties().setGameMode(gamemodeInput);
                src.sendMessage(Util.getTextMessageWithFormat("command.world.setgamemode.success"));
            } else {
                src.sendMessage(Util.getTextMessageWithFormat("command.world.player"));
                return CommandResult.empty();
            }
        }

        return CommandResult.success();
    }
}

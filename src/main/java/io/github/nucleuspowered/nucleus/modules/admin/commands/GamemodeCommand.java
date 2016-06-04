/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedGameModeArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;

@Permissions
@NoCooldown
@NoWarmup
@NoCost
@RegisterCommand({"gamemode", "gm"})
public class GamemodeCommand extends CommandBase<CommandSource> {

    private final String userKey = "user";
    private final String gamemodeKey = "gamemode";

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mpi = Maps.newHashMap();
        mpi.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.gamemode.other"), SuggestedLevel.ADMIN));
        return mpi;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                GenericArguments.optionalWeak(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.user(Text.of(userKey))), permissions.getPermissionWithSuffix("others"))),
                GenericArguments.optional(GenericArguments.onlyOne(new ImprovedGameModeArgument(Text.of(gamemodeKey))))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> ou = this.getUser(Player.class, src, userKey, args);
        if (!ou.isPresent()) {
            return CommandResult.empty();
        }

        Player user = ou.get();
        Optional<GameMode> ogm = args.getOne(gamemodeKey);
        if (!ogm.isPresent()) {
            String mode = user.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL).getName();
            if (src.equals(user)) {
                src.sendMessage(Util.getTextMessageWithFormat("command.gamemode.get.base", mode));
            } else {
                src.sendMessage(Util.getTextMessageWithFormat("command.gamemode.get.other", user.getName(), mode));
            }

            return CommandResult.success();
        }

        GameMode gm = ogm.get();
        DataTransactionResult dtr = user.offer(Keys.GAME_MODE, gm);
        if (dtr.isSuccessful()) {
            if (!src.equals(user)) {
                src.sendMessage(Util.getTextMessageWithFormat("command.gamemode.set.other", user.getName(), gm.getName()));
            }

            user.sendMessage(Util.getTextMessageWithFormat("command.gamemode.set.base", gm.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.gamemode.error", user.getName()));
        return CommandResult.empty();
    }
}

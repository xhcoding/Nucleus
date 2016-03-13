/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.admin;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedGameModeParser;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;

@Permissions
@Modules(PluginModule.ADMIN)
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
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.optionalWeak(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.user(Text.of(userKey))), permissions.getPermissionWithSuffix("others"))),
                GenericArguments.optional(GenericArguments.onlyOne(new ImprovedGameModeParser(Text.of(gamemodeKey))))
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> ou = this.getUser(Player.class, src, userKey, args);
        if (!ou.isPresent()) {
            return CommandResult.empty();
        }

        Player user = ou.get();
        Optional<GameMode> ogm = args.<GameMode>getOne(gamemodeKey);
        if (!ogm.isPresent()) {
            String mode = user.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL).getName();
            if (src.equals(user)) {
                src.sendMessage(Util.getTextMessageWithFormat("command.gamemode.get", mode));
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

            user.sendMessage(Util.getTextMessageWithFormat("command.gamemode.set", gm.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.gamemode.error", user.getName()));
        return CommandResult.empty();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions
@RegisterCommand({"ignite", "burn"})
public class IgniteCommand extends OldCommandBase<CommandSource> {

    private final String player = "player";
    private final String ticks = "ticks";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.others", this.getAliases()[0]), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return getSpecBuilderBase()
                .arguments(GenericArguments.seq(
                        GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                                GenericArguments.onlyOne(GenericArguments.player(Text.of(player))), permissions.getPermissionWithSuffix("others"))),
                GenericArguments.onlyOne(GenericArguments.integer(Text.of(ticks))))).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource pl, CommandContext args) throws Exception {
        int ticksInput = args.<Integer>getOne(ticks).get();
        Optional<Player> opl = this.getUser(Player.class, pl, player, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        Player target = opl.get();
        GameMode gm = target.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL);
        if (gm == GameModes.CREATIVE || gm == GameModes.SPECTATOR) {
            pl.sendMessage(Util.getTextMessageWithFormat("command.ignite.gamemode", target.getName()));
            return CommandResult.empty();
        }

        if (target.offer(Keys.FIRE_TICKS, ticksInput).isSuccessful()) {
            pl.sendMessage(Util.getTextMessageWithFormat("command.ignite.success", opl.get().getName(), String.valueOf(ticksInput)));
            return CommandResult.success();
        } else {
            pl.sendMessage(Util.getTextMessageWithFormat("command.ignite.error", opl.get().getName()));
            return CommandResult.empty();
        }
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

@Permissions(supportsOthers = true)
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand("ping")
public class PingCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    @Override protected CommandResult executeWithPlayer(CommandSource source, Player target, CommandContext args, boolean isSelf) {
        if (isSelf) {
            source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ping.current.self",
                String.valueOf(target.getConnection().getLatency())));
        } else {
            source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ping.current.other",
                target.getName(), String.valueOf(target.getConnection().getLatency())));
        }

        return CommandResult.success();
    }
}

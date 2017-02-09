/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

@Permissions(supportsOthers = true)
@RegisterCommand("heal")
public class HealCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    private static final String player = "subject";

    @Override protected CommandResult executeWithPlayer(CommandSource src, Player pl, CommandContext args, boolean isSelf) throws Exception {
        if (pl.offer(Keys.HEALTH, pl.get(Keys.MAX_HEALTH).get()).isSuccessful()) {
            pl.offer(Keys.FIRE_TICKS, 0);
            pl.sendMessages(plugin.getMessageProvider().getTextMessageWithFormat("command.heal.success.self"));
            if (!isSelf) {
                src.sendMessages(plugin.getMessageProvider().getTextMessageWithFormat("command.heal.success.other", pl.getName()));
            }

            return CommandResult.success();
        } else {
            src.sendMessages(plugin.getMessageProvider().getTextMessageWithFormat("command.heal.error"));
            return CommandResult.empty();
        }
    }
}

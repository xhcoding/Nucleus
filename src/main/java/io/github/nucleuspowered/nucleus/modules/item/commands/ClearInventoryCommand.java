/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands;

import io.github.nucleuspowered.nucleus.Util;
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

@RegisterCommand({"clear", "clearinv", "clearinventory", "ci"})
@NoCooldown
@NoWarmup
@NoCost
@Permissions(supportsOthers = true)
public class ClearInventoryCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    private final String player = "subject";

    @Override protected CommandResult executeWithPlayer(CommandSource source, Player target, CommandContext args, boolean isSelf) throws Exception {
        Util.getStandardInventory(target).clear();
        source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.clearinventory.success", target.getName()));
        return CommandResult.success();
    }
}

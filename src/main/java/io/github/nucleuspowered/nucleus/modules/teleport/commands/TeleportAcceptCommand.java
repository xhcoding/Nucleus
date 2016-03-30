/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;

/**
 * /tpaccept.
 */
@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.USER)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"tpaccept", "teleportaccept"})
public class TeleportAcceptCommand extends OldCommandBase<Player> {

    @Inject private TeleportHandler teleportHandler;

    @Override
    public CommandSpec createSpec() {
        return getSpecBuilderBase().build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (teleportHandler.getAndExecute(src.getUniqueId())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.tpaccept.success"));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.tpaccept.nothing"));
        return CommandResult.empty();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

/**
 * /tpaccept.
 */
@Permissions(prefix = "teleport", suggestedLevel = SuggestedLevel.USER)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"tpaccept", "teleportaccept", "tpyes"})
@EssentialsEquivalent({"tpaccept", "tpyes"})
public class TeleportAcceptCommand extends AbstractCommand<Player> {

    @Inject private TeleportHandler teleportHandler;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (teleportHandler.getAndExecute(src.getUniqueId())) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tpaccept.success"));
            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tpaccept.nothing"));
        return CommandResult.empty();
    }
}

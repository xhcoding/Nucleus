/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * NOTE: TeleportHere is considered an admin command, as there is a potential for abuse for non-admin players trying to
 * pull players. No cost or warmups will be applied. /tpahere should be used instead in these circumstances.
 */
@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.ADMIN)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({ "tphere", "tph" })
public class TeleportHereCommand extends CommandBase<Player> {

    private final String playerKey = "player";

    @Inject private TeleportHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
            GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey)))
        ).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player target = args.<Player>getOne(playerKey).get();
        handler.getBuilder().setFrom(target).setTo(src).startTeleport();
        return CommandResult.success();
    }
}

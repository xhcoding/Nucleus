/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
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
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({ "tphere", "tph" })
public class TeleportHereCommand extends CommandBase<Player> {

    private final String playerKey = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
            GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey)))
        ).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player target = args.<Player>getOne(playerKey).get();
        plugin.getTpHandler().getBuilder().setFrom(target).setTo(src).startTeleport();
        return CommandResult.success();
    }
}

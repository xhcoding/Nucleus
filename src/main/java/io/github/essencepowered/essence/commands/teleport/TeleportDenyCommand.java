/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import com.google.inject.Inject;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * /tpdeny.
 */
@Modules(PluginModule.TELEPORT)
@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.USER)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"tpdeny", "teleportdeny"})
public class TeleportDenyCommand extends CommandBase<Player> {
    @Inject private TeleportHandler teleportHandler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        boolean denied = teleportHandler.remove(src.getUniqueId());
        src.sendMessage(Text.of(denied ? TextColors.GREEN : TextColors.RED, Util.getMessageWithFormat(denied ? "command.tpdeny.deny" : "command.tpdeny.fail")));
        return CommandResult.success();
    }
}

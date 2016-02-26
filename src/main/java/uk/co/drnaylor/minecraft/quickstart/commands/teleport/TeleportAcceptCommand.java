/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandPermissionHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.TeleportHandler;

/**
 * /tpaccept.
 */
@Modules(PluginModule.TELEPORT)
@Permissions(root = "teleport", suggestedLevel = CommandPermissionHandler.SuggestedLevel.USER)
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class TeleportAcceptCommand extends CommandBase<Player> {
    @Inject private TeleportHandler teleportHandler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "tpaccept", "teleportaccept" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (teleportHandler.getAndExecute(src.getUniqueId())) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tpaccept.success")));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.tpaccept.nothing")));
        return CommandResult.empty();
    }
}

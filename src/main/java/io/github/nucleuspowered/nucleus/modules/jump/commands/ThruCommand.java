/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jump.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.jump.config.JumpConfigAdapter;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

@Permissions
@RegisterCommand({"thru", "through"})
public class ThruCommand extends CommandBase<Player> {

    @Inject private JumpConfigAdapter jca;

    @Override
    public CommandElement[] getArguments() {
        return super.getArguments();
    }

    // Original code taken from EssentialCmds. With thanks to 12AwsomeMan34 for
    // the initial contribution.
    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        BlockRay<World> playerBlockRay = BlockRay.from(player).blockLimit(jca.getNode().getMaxThru()).build();
        World world = player.getWorld();

        // First, see if we get a wall.
        while (playerBlockRay.hasNext()) {
            // Once we have a wall, we'll break out.
            if (!world.getBlockType(playerBlockRay.next().getBlockPosition()).equals(BlockTypes.AIR)) {
                break;
            }
        }

        // Even if we did find a wall, no good if we are at the end of the ray.
        if (!playerBlockRay.hasNext()) {
            player.sendMessage(Util.getTextMessageWithFormat("command.thru.nowall"));
            return CommandResult.empty();
        }

        do {
            BlockRayHit<World> b = playerBlockRay.next();
            if (player.getWorld().getBlockType(b.getBlockPosition()).equals(BlockTypes.AIR)) {
                // If we can go, do so.
                if (player.setLocationSafely(b.getLocation())) {
                    player.sendMessage(Util.getTextMessageWithFormat("command.thru.success"));
                    return CommandResult.success();
                } else {
                    player.sendMessage(Util.getTextMessageWithFormat("command.thru.notsafe"));
                    return CommandResult.empty();
                }
            }
        } while (playerBlockRay.hasNext());

        player.sendMessage(Util.getTextMessageWithFormat("command.thru.nospot"));
        return CommandResult.empty();
    }
}

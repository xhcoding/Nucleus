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
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Permissions
@RegisterCommand({"jump", "j", "jmp"})
public class JumpCommand extends CommandBase<Player> {

    @Inject private JumpConfigAdapter jca;

    @Override
    public CommandElement[] getArguments() {
        return super.getArguments();
    }

    // Original code taken from EssentialCmds. With thanks to 12AwsomeMan34 for
    // the initial contribution.
    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        BlockRay<World> playerBlockRay = BlockRay.from(player).blockLimit(jca.getNode().getMaxJump()).build();

        BlockRayHit<World> finalHitRay = null;

        // Iterate over the blocks until we get a solid block.
        while (finalHitRay == null && playerBlockRay.hasNext()) {
            BlockRayHit<World> currentHitRay = playerBlockRay.next();
            if (!player.getWorld().getBlockType(currentHitRay.getBlockPosition()).equals(BlockTypes.AIR)) {
                finalHitRay = currentHitRay;
            }
        }

        if (finalHitRay == null) {
            // We didn't find anywhere to jump to.
            player.sendMessage(Util.getTextMessageWithFormat("command.jump.noblock"));
            return CommandResult.empty();
        }

        // If the block not passable, then it is a solid block
        Location<World> finalLocation = finalHitRay.getLocation();
        if (!finalHitRay.getLocation().getProperty(PassableProperty.class).get().getValue()) {
            finalLocation = finalLocation.add(0, 1, 0);
        } else if (finalHitRay.getLocation().getRelative(Direction.DOWN).getProperty(PassableProperty.class).get().getValue()) {
            finalLocation = finalLocation.sub(0, 1, 0);
        }

        if (player.setLocationSafely(finalLocation)) {
            player.sendMessage(Util.getTextMessageWithFormat("command.jump.success"));
            return CommandResult.success();
        }

        player.sendMessage(Util.getTextMessageWithFormat("command.jump.notsafe"));
        return CommandResult.empty();
    }
}

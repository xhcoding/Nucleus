/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Optional;

@Permissions
@RegisterCommand({ "blockinfo" })
@RunAsync
public class BlockInfoCommand extends CommandBase<Player> {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        BlockRay<World> bl = BlockRay.from(player).blockLimit(10).filter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1)).build();
        Optional<BlockRayHit<World>> ob = bl.end();

        // If the last block is not air...
        if (ob.isPresent() && ob.get().getLocation().getBlockType() != BlockTypes.AIR) {
            BlockRayHit<World> brh = ob.get();

            // get the information.
            BlockState b = brh.getLocation().getBlock();
            BlockType it = b.getType();
            player.sendMessage(Util.getTextMessageWithFormat("command.blockinfo.id", it.getId(), it.getName()));

            Collection<BlockTrait<?>> cb = b.getTraits();
            if (!cb.isEmpty()) {
                cb.forEach(x -> b.getTraitValue(x).ifPresent(v ->
                    player.sendMessage(Util.getTextMessageWithFormat("command.blockinfo.traits.item", x.getName(), v.toString()))
                ));
            }

            return CommandResult.success();
        }

        player.sendMessage(Util.getTextMessageWithFormat("command.blockinfo.none"));
        return CommandResult.empty();
    }
}

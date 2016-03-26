/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

@Permissions
@RegisterCommand({"more", "stack"})
public class MoreCommand extends CommandBase<Player> {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {

        if (player.getItemInHand().isPresent())
        {
            ItemStack stack = player.getItemInHand().get();
            stack.setQuantity(stack.getMaxStackQuantity());
            player.setItemInHand(stack);
            player.sendMessage(Util.getTextMessageWithFormat("command.more.success", stack.getItem().getName(), String.valueOf(stack.getItem().getMaxStackQuantity())));
            return CommandResult.success();
        }

        player.sendMessage(Util.getTextMessageWithFormat("command.more.none"));
        return CommandResult.empty();
    }
}

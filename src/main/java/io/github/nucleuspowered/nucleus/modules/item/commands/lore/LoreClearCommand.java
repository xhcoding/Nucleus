/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.lore;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "lore", mainOverride = "set")
@NonnullByDefault
@RegisterCommand(value = "clear", subcommandOf = LoreCommand.class)
public class LoreClearCommand extends AbstractCommand<Player> {

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (!src.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            throw ReturnMessageException.fromKey("command.lore.clear.noitem");
        }

        ItemStack stack = src.getItemInHand(HandTypes.MAIN_HAND).get();
        LoreData loreData = stack.getOrCreate(LoreData.class).get();
        if (loreData.lore().isEmpty()) {
            throw ReturnMessageException.fromKey("command.lore.clear.none");
        }

        if (stack.remove(LoreData.class).isSuccessful()) {
            src.setItemInHand(HandTypes.MAIN_HAND, stack);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.lore.clear.success"));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.lore.clear.fail");
    }

}

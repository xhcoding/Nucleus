/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.type.GridInventory;

import javax.inject.Inject;

@Permissions
@NoCost
@NoCooldown
@NoWarmup
@RegisterCommand(value = "redeem", subcommandOf = FirstKitCommand.class)
public class FirstKitRedeemCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<Player> {

    @Inject
    private GeneralService gds;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        int reject = 0;

        Inventory target = src.getInventory().query(Hotbar.class, GridInventory.class);
        for (ItemStackSnapshot x : gds.getFirstKit()) {
            if (x.getType() != ItemTypes.NONE) {
                reject += target.offer(x.createStack()).getRejectedItems().size();
            }
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.redeem.success"));
        if (reject > 0) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.redeem.reject"));
        }

        return CommandResult.success();
    }
}

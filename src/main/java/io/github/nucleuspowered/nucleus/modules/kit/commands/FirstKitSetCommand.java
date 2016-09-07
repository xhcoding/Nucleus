/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Permissions
@NoCost
@NoCooldown
@NoWarmup
@RegisterCommand(value = "set", subcommandOf = FirstKitCommand.class)
public class FirstKitSetCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<Player> {

    @Inject
    private GeneralService gds;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        List<Inventory> slots = Lists.newArrayList(src.getInventory().slots());
        final List<ItemStackSnapshot> stacks = slots.stream().filter(x -> x.peek().isPresent()).map(x -> x.peek().get().createSnapshot()).collect(Collectors.toList());
        gds.setFirstKit(stacks);

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.set.success"));
        return CommandResult.success();
    }
}

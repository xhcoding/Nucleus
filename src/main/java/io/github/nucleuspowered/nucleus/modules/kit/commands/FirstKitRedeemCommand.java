/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.KitService;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tristate;

import javax.inject.Inject;

@Permissions(prefix = "firstjoinkit")
@NoCost
@NoCooldown
@NoWarmup
@RegisterCommand(value = "redeem", subcommandOf = FirstKitCommand.class)
public class FirstKitRedeemCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<Player> {

    @Inject
    private KitService gds;

    @Inject private KitConfigAdapter kca;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {

        Tristate tristate = Util.addToStandardInventory(src, gds.getFirstKit(), false, kca.getNodeOrDefault().isProcessTokens());

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.redeem.success"));
        if (tristate != Tristate.TRUE) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.redeem.reject"));
        }

        return CommandResult.success();
    }
}

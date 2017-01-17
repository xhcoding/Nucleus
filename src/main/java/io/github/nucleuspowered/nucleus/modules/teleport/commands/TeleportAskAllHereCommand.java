/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

@Permissions(prefix = "teleport")
@NoWarmup
@NoCost
@NoCooldown
@RegisterCommand({"tpaall", "tpaskall"})
@RunAsync
public class TeleportAskAllHereCommand extends AbstractCommand<Player> {

    @Inject private TeleportHandler tpHandler;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.flags().flag("f").buildWith(GenericArguments.none())};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Sponge.getServer().getOnlinePlayers().forEach(x -> {
            if (x.equals(src)) {
                return;
            }

            TeleportHandler.TeleportBuilder tb = tpHandler.getBuilder().setFrom(x).setTo(src).setSafe(!args.<Boolean>getOne("f").orElse(false))
                    .setBypassToggle(true).setSilentSource(true);
            tpHandler.addAskQuestion(x.getUniqueId(), new TeleportHandler.TeleportPrep(Instant.now().plus(30, ChronoUnit.SECONDS), null, 0, tb));

            x.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tpahere.question", src.getName()));

            x.sendMessage(tpHandler.getAcceptDenyMessage());
        });

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tpaall.success"));
        return CommandResult.success();
    }
}

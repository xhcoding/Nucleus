/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.NoCooldown;
import io.github.essencepowered.essence.internal.annotations.NoCost;
import io.github.essencepowered.essence.internal.annotations.NoWarmup;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.annotations.RunAsync;
import io.github.essencepowered.essence.internal.services.TeleportHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCost
@NoCooldown
@RegisterCommand({"tpaall", "tpaskall"})
@RunAsync
public class TeleportAskAllHereCommand extends CommandBase<Player> {

    @Inject private TeleportHandler tpHandler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.flags().flag("f").buildWith(GenericArguments.none())).executor(this).build();
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

            x.sendMessage(Util.getTextMessageWithFormat("command.tpahere.question", src.getName()));

            x.sendMessage(tpHandler.getAcceptDenyMessage());
        });

        src.sendMessage(Util.getTextMessageWithFormat("command.tpaall.success"));
        return CommandResult.success();
    }
}

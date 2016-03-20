/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;

import javax.inject.Inject;

@Permissions(root = "teleport")
@NoWarmup
@NoCost
@NoCooldown
@RegisterCommand({"tpall", "tpallhere"})
public class TeleportAllHereCommand extends CommandBase<Player> {

    @Inject private TeleportHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.flags().flag("f").buildWith(GenericArguments.none())).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        MessageChannel.TO_ALL.send(Util.getTextMessageWithFormat("command.tpall.broadcast", src.getName()));
        Sponge.getServer().getOnlinePlayers().forEach(x -> {
            if (!x.equals(src)) {
                try {
                    handler.getBuilder().setFrom(x).setTo(src).setSafe(!args.<Boolean>getOne("f").orElse(false)).setSilentSource(true)
                            .setBypassToggle(true).startTeleport();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return CommandResult.success();
    }
}

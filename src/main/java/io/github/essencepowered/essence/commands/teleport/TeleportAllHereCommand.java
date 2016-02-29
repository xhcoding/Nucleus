/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.services.TeleportHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCost
@NoCooldown
@RegisterCommand({ "tpall", "tpallhere" })
public class TeleportAllHereCommand extends CommandBase<Player> {
    @Inject private TeleportHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.flags().flag("f").buildWith(GenericArguments.none())).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        MessageChannel.TO_ALL.send(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tpall.broadcast", src.getName())));
        Sponge.getServer().getOnlinePlayers().forEach(x -> {
            if (!x.equals(src)) {
                try {
                    handler.getBuilder().setFrom(x).setTo(src).setSafe(!args.<Boolean>getOne("f").orElse(false)).setSilentSource(true).setBypassToggle(true).startTeleport();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return CommandResult.success();
    }
}

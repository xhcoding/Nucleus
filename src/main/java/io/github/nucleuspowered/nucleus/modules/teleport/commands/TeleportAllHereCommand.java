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
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@Permissions(prefix = "teleport")
@NoWarmup
@NoCost
@NoCooldown
@RegisterCommand({"tpall", "tpallhere"})
@EssentialsEquivalent("tpall")
public class TeleportAllHereCommand extends AbstractCommand<Player> {

    @Inject private TeleportHandler handler;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.flags().flag("f").buildWith(GenericArguments.none())};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        MessageChannel.TO_ALL.send(plugin.getMessageProvider().getTextMessageWithFormat("command.tpall.broadcast", src.getName()));
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

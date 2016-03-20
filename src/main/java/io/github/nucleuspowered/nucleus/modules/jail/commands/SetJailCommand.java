/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@Permissions(root = "jail")
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand(value = "set", subcommandOf = JailsCommand.class)
public class SetJailCommand extends CommandBase<Player> {

    private final String jailName = "jail";
    @Inject private JailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of(jailName)))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        String name = args.<String>getOne(jailName).get().toLowerCase();
        if (handler.getJail(name).isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.jails.set.exists", name));
            return CommandResult.empty();
        }

        if (handler.setJail(name, src.getLocation(), src.getRotation())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.jails.set.success", name));
            return CommandResult.success();
        } else {
            src.sendMessage(Util.getTextMessageWithFormat("command.jails.set.error", name));
            return CommandResult.empty();
        }
    }
}

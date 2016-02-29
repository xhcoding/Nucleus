/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.admin;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;

@Modules(PluginModule.ADMIN)
@Permissions
@RegisterCommand("sudo")
public class SudoCommand extends CommandBase<CommandSource> {
    private final String playerKey = "player";
    private final String commandKey = "command";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey))),
                GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(commandKey)))
        ).executor(this).build();
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.target", new PermissionInformation(Util.getMessageWithFormat("permission.sudo.exempt"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(playerKey).get();
        String cmd = args.<String>getOne(commandKey).get();
        if (pl.equals(src) || permissions.testSuffix(src, "exempt.target")) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.sudo.noperms")));
            return CommandResult.empty();
        }

        if (cmd.startsWith("c:")) {
            if (cmd.equals("c:")) {
                src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.sudo.chatfail")));
                return CommandResult.empty();
            }

            MutableMessageChannel mmc = MessageChannel.TO_ALL.asMutable();
            mmc.send(pl, Text.of(cmd.split(":", 2)[1]));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.sudo.force", pl.getName(), cmd)));
        Sponge.getCommandManager().process(pl, cmd);
        return CommandResult.success();
    }
}

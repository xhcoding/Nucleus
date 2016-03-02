/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.kick;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.NoCooldown;
import io.github.essencepowered.essence.internal.annotations.NoCost;
import io.github.essencepowered.essence.internal.annotations.NoWarmup;
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
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;

/**
 * Kicks all players
 *
 * Permission: quickstart.kickall.base
 */
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@Modules(PluginModule.KICKS)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand("kickall")
public class KickAllCommand extends CommandBase<CommandSource> {

    private final String reason = "reason";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Kicks all players.")).executor(this)
                .arguments(
                        GenericArguments.requiringPermission(GenericArguments.flags().flag("f").buildWith(GenericArguments.none()),
                                permissions.getPermissionWithSuffix("whitelist")),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason))))).build();
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("whitelist", new PermissionInformation(Util.getMessageWithFormat("permission.kickall.whitelist"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String r = args.<String>getOne(reason).orElse(Util.getMessageWithFormat("command.kick.defaultreason"));
        Boolean f = args.<Boolean>getOne("f").orElse(false);

        if (f) {
            Sponge.getServer().setHasWhitelist(true);
        }

        Sponge.getServer().getOnlinePlayers().stream().filter(x -> !(src instanceof Player) || ((Player) src).getUniqueId().equals(x.getUniqueId()))
                .forEach(x -> x.kick(Text.of(TextColors.RED, r)));

        MessageChannel mc = MessageChannel.fixed(Sponge.getServer().getConsole(), src);
        mc.send(Util.getTextMessageWithFormat("command.kickall.message"));
        mc.send(Util.getTextMessageWithFormat("command.reason", r));
        if (f) {
            mc.send(Util.getTextMessageWithFormat("command.kickall.whitelist"));
        }

        return CommandResult.success();
    }
}

/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.kick;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.enums.SuggestedLevel;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Kicks a player
 *
 * Permission: quickstart.kick.base
 */
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@Modules(PluginModule.KICKS)
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class KickCommand extends CommandBase {
    private final String player = "player";
    private final String reason = "reason";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Kicks a player.")).executor(this)
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.player(Text.of(player))),
                        GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason))))
                ).build();
    }

    @Override
    public Map<String, SuggestedLevel> permissionSuffixesToRegister() {
        Map<String, SuggestedLevel> m = new HashMap<>();
        m.put("notify", SuggestedLevel.MOD);
        return m;
    }

    @Override
    public String[] getAliases() {
        return new String[] { "kick" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(player).get();
        String r = args.<String>getOne(reason).orElse(Util.getMessageWithFormat("command.kick.defaultreason"));
        pl.kick(Text.of(r));

        MessageChannel mc = MessageChannel.permission(permissions.getPermissionWithSuffix("notify"));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.kick.message"), pl.getName(), src.getName())));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.reason"), reason)));
        return CommandResult.success();
    }
}

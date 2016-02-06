package uk.co.drnaylor.minecraft.quickstart.commands.kick;

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
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kicks all players
 *
 * Permission: quickstart.kickall.base
 */
@Permissions(includeMod = true)
@Modules(PluginModule.KICKS)
@NoWarmup
@NoCooldown
@NoCost
public class KickAllCommand extends CommandBase {
    private final String reason = "reason";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Kicks all players.")).executor(this)
            .arguments(
                    GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason))))
            ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "kickall" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String r = args.<String>getOne(reason).orElse(Util.messageBundle.getString("command.kick.defaultreason"));

        Sponge.getServer().getOnlinePlayers().stream().filter(x -> !(src instanceof Player) || ((Player) src).getUniqueId().equals(x.getUniqueId())).forEach(x -> x.kick(Text.of(TextColors.RED, r)));

        List<CommandSource> lcs = Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.hasPermission(PermissionUtil.PERMISSIONS_PREFIX + "kick.notify"))
                .map(y -> (CommandSource)y).collect(Collectors.toList());
        lcs.add(Sponge.getServer().getConsole());

        MessageChannel mc = MessageChannel.fixed(Sponge.getServer().getConsole(), src);
        mc.send(Text.of(TextColors.GREEN, Util.messageBundle.getString("command.kickall.message")));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.reason"), r)));
        return CommandResult.success();
    }
}

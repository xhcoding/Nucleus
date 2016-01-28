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
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kicks a player
 *
 * Permission: quickstart.kick.base
 */
@Permissions
@Modules(PluginModule.KICKS)
@NoWarmup
@NoCooldown
@NoCost
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
    public String[] getAliases() {
        return new String[] { "kick" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(player).get();
        String r = args.<String>getOne(reason).orElse(Util.messageBundle.getString("command.kick.defaultreason"));
        pl.kick(Text.of(r));

        List<CommandSource> lcs = Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.hasPermission(QuickStart.PERMISSIONS_ADMIN) && x.hasPermission(QuickStart.PERMISSIONS_PREFIX + "kick.notify"))
                .map(y -> (CommandSource)y).collect(Collectors.toList());
        lcs.add(Sponge.getServer().getConsole());

        MessageChannel mc = MessageChannel.fixed(lcs);
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.kick.message"), pl.getName(), src.getName())));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.reason"), reason)));
        return CommandResult.success();
    }
}

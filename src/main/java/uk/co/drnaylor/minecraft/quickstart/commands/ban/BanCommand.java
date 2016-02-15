package uk.co.drnaylor.minecraft.quickstart.commands.ban;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.util.Set;

@RootCommand
@Permissions(includeMod = true)
@Modules(PluginModule.BANS)
@NoWarmup
@NoCooldown
@NoCost
public class BanCommand extends CommandBase {
    private final String user = "user";
    private final String reason = "reason";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new UserParser(Text.of(user))),
                GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of(reason)))).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "ban" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User u = args.<User>getOne(user).get();
        String r = args.<String>getOne(reason).orElse(Util.messageBundle.getString("ban.defaultreason"));

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        if (service.isBanned(u.getProfile())) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.ban.alreadyset", u.getName())));
            return CommandResult.empty();
        }

        // Create the ban.
        Ban bp = Ban.builder().profile(u.getProfile()).source(src).reason(Text.of(r)).type(BanTypes.PROFILE).build();
        service.addBan(bp);

        Player pl = null;
        if (src instanceof Player) {
            pl = (Player) src;
        }

        // Get the permission, "quickstart.ban.notify"
        Set<String> notify = permissions.getPermissionWithSuffix("notify");
        notify.add(PermissionUtil.PERMISSIONS_MOD);
        MessageChannel send = Util.getMessageChannel(pl, notify.toArray(new String[notify.size()]));

        send.send(Text.of(TextColors.RED, Util.getMessageWithFormat("command.ban.applied", u.getName(), src.getName())));
        send.send(Text.of(TextColors.RED, Util.getMessageWithFormat("standard.reason", reason)));

        return CommandResult.success();
    }
}

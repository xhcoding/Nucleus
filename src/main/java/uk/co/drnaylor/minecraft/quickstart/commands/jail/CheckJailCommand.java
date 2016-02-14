package uk.co.drnaylor.minecraft.quickstart.commands.jail;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.JailHandler;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Permissions(root = "jail", includeMod = true)
@RunAsync
@Modules(PluginModule.JAILS)
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class CheckJailCommand extends CommandBase {
    private final String playerKey = "playerKey";
    @Inject
    private JailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(GenericArguments.onlyOne(new UserParser(Text.of(playerKey)))).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "checkjail" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();
        Optional<JailData> jail = handler.getPlayerJailData(user);

        if (!jail.isPresent()) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.checkjail.nojail", user.getName())));
            return CommandResult.success();
        }

        JailData md = jail.get();
        String name;
        if (md.getJailer().equals(Util.consoleFakeUUID)) {
            name = Sponge.getServer().getConsole().getName();
        } else {
            Optional<User> ou = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(md.getJailer());
            name = ou.isPresent() ? ou.get().getName() : Util.messageBundle.getString("standard.unknown");
        }

        String time = "";
        String forString = "";
        if (md.getEndTimestamp().isPresent()) {
            time = Util.getTimeStringFromSeconds(Instant.now().until(md.getEndTimestamp().get(), ChronoUnit.SECONDS));
            forString = " " + Util.messageBundle.getString("standard.for") + " ";
        } else if (md.getTimeFromNextLogin().isPresent()) {
            time = Util.getTimeStringFromSeconds(md.getTimeFromNextLogin().get().getSeconds());
            forString = " " + Util.messageBundle.getString("standard.for") + " ";
        }

        src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.checkjail.jailed"), user.getName(), md.getJailName(), name, forString, time)));
        src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("standard.reason"), md.getReason())));
        return CommandResult.success();
    }
}

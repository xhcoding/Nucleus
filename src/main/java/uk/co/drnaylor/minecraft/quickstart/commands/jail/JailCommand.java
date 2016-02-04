package uk.co.drnaylor.minecraft.quickstart.commands.jail;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.JailParser;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.TimespanParser;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.JailHandler;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

// quickstart.jail.notify
@Permissions
@Modules(PluginModule.JAILS)
@NoWarmup
@NoCooldown
@NoCost
public class JailCommand extends CommandBase {
    @Inject private JailHandler handler;
    private final String playerKey = "player";
    private final String jailKey = "jail";
    private final String durationKey = "duration";
    private final String reasonKey = "reason";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
            GenericArguments.onlyOne(new UserParser(Text.of(playerKey))),
            GenericArguments.optional(GenericArguments.onlyOne(new JailParser(Text.of(jailKey), handler))),
            GenericArguments.optionalWeak(GenericArguments.onlyOne(new TimespanParser(Text.of(durationKey)))),
            GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reasonKey))))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "jail", "unjail", "togglejail" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Get the player.
        User pl = args.<User>getOne(playerKey).get();
        if (handler.isPlayerJailed(pl)) {
            return onUnjail(src, args, pl);
        } else {
            return onJail(src, args, pl);
        }
    }

    private CommandResult onUnjail(CommandSource src, CommandContext args, User user) throws Exception {
        if (handler.unjailPlayer(user)) {
            return CommandResult.success();
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.jail.unjail.fail", user.getName())));
            return CommandResult.empty();
        }
    }

    private CommandResult onJail(CommandSource src, CommandContext args, User user) throws Exception {
        Optional<WarpLocation> owl = args.<WarpLocation>getOne(jailKey);
        if (!owl.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.jail.jail.nojail")));
            return CommandResult.empty();
        }

        // This might not be there.
        Optional<Long> duration = args.getOne(durationKey);
        String reason = args.<String>getOne(reasonKey).orElse(Util.messageBundle.getString("command.jail.reason"));
        JailData jd;
        Text message;
        Text messageTo;
        if (duration.isPresent()) {
            if (user.isOnline()) {
                jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, user.getPlayer().get().getLocation(), Instant.now().plusSeconds(duration.get()));
            } else {
                jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, null, Duration.of(duration.get(), ChronoUnit.SECONDS));
            }

            message = Text.of(TextColors.GREEN,
                    Util.getMessageWithFormat("command.checkjail.jailed",
                            user.getName(), owl.get().getName(), src.getName(), Util.messageBundle.getString("standard.for"), Util.getTimeStringFromSeconds(duration.get())));
            messageTo = Text.of(TextColors.RED,
                    Util.getMessageWithFormat("command.jail.jailed",
                            owl.get().getName(), src.getName(), Util.messageBundle.getString("standard.for"), Util.getTimeStringFromSeconds(duration.get())));
        } else {
            jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, user.isOnline() ? user.getPlayer().get().getLocation() : null);
            message = Text.of(TextColors.GREEN,
                    Util.getMessageWithFormat("command.checkjail.jailed",
                            user.getName(), owl.get().getName(), src.getName(), "", ""));
            messageTo = Text.of(TextColors.RED,
                    Util.getMessageWithFormat("command.jail.jailed",
                            owl.get().getName(), src.getName(), "", ""));
        }

        if (handler.jailPlayer(user, jd)) {
            MutableMessageChannel mmc = MessageChannel.permission(QuickStart.PERMISSIONS_PREFIX + "jail.notify").asMutable();
            mmc.addMember(Sponge.getServer().getConsole());
            mmc.send(message);
            mmc.send(Text.of(TextColors.GREEN, Util.getMessageWithFormat("standard.reason", reason)));

            if (user.isOnline()) {
                user.getPlayer().get().sendMessage(messageTo);
                user.getPlayer().get().sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("standard.reason", reason)));
            }

            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.jail.error")));
        return CommandResult.empty();
    }
}

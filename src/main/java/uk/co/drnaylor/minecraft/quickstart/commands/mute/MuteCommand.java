package uk.co.drnaylor.minecraft.quickstart.commands.mute;

import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.data.mute.MuteData;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.TimespanParser;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mutes or unmutes a player.
 *
 * Command Usage: /mute user [time] [reason]
 * Permission: quickstart.mute.base
 */
@Permissions
@RunAsync
@Modules(PluginModule.MUTES)
@NoWarmup
@NoCooldown
@NoCost
public class MuteCommand extends CommandBase {

    @Inject private UserConfigLoader userConfigLoader;

    private final String permission = QuickStart.PERMISSIONS_PREFIX + "mute.notify";

    private String playerArgument = "Player";
    private String timespanArgument = "Time";
    private String reason = "Reason";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Mutes or unmutes a player")).executor(this).arguments(
                GenericArguments.onlyOne(new UserParser(Text.of(playerArgument))),
                GenericArguments.onlyOne(GenericArguments.optionalWeak(new TimespanParser(Text.of(timespanArgument)))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason))))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "mute", "unmute" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {

        // Get the user.
        User user = args.<User>getOne(playerArgument).get();
        QuickStartUser uc;
        try {
            uc = userConfigLoader.getUser(user);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            throw new CommandException(Text.of(TextColors.RED, Util.messageBundle.getString("command.file.load")), e);
        }

        Optional<Long> time = args.<Long>getOne(timespanArgument);
        Optional<MuteData> omd = uc.getMuteData();
        Optional<String> reas = args.<String>getOne(reason);

        // No time, no reason, but is muted, unmute
        if (omd.isPresent() && !time.isPresent() && !reas.isPresent()) {
            // Unmute.
            uc.removeMuteData();
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.unmute.success"), user.getName(), src.getName())));
            return CommandResult.success();
        }

        // Do we have a time?
        String rs = reas.orElse(Util.messageBundle.getString("command.mute.defaultreason"));
        UUID ua = Util.consoleFakeUUID;
        if (src instanceof Player) {
            ua = ((Player) src).getUniqueId();
        }

        List<CommandSource> lmc = Sponge.getServer().getOnlinePlayers().stream()
                .filter(x -> x.hasPermission(permission) || x.hasPermission(QuickStart.PERMISSIONS_ADMIN)).collect(Collectors.toList());
        lmc.add(Sponge.getServer().getConsole());

        MessageChannel mc = MessageChannel.fixed(lmc);

        MuteData md;
        if (time.isPresent()) {
            md = user.isOnline() ? onlineTimedMute(src, user, time.get(), rs, ua, mc) : offlineTimedMute(src, user, time.get(), rs, ua, mc);
        } else {
            md = permMute(src, user, rs, ua, mc);
        }

        uc.setMuteData(md);
        return CommandResult.success();
    }

    private MuteData onlineTimedMute(CommandSource src, User user, long time, String rs, UUID ua, MessageChannel mc) {
        String ts = Util.getTimeStringFromSeconds(time);
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.mute.success.time"),
                user.getName(), src.getName(), ts)));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("standard.reason"), rs)));

        user.getPlayer().get().sendMessage(Text.of(TextColors.RED, MessageFormat.format(Util.messageBundle.getString("mute.playernotify.time"), ts)));
        user.getPlayer().get().sendMessage(Text.of(TextColors.RED, MessageFormat.format(Util.messageBundle.getString("command.reason"), rs)));
        return new MuteData(ua, new Date().getTime() + (time * 1000), rs);
    }

    private MuteData offlineTimedMute(CommandSource src, User user, long time, String rs, UUID ua, MessageChannel mc) {
        String ts = Util.getTimeStringFromSeconds(time);
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.mute.success.time"),
                user.getName(), src.getName(), ts)));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("standard.reason"), rs)));
        return new MuteData(ua, rs, time * 1000);
    }

    private MuteData permMute(CommandSource src, User user, String rs, UUID ua, MessageChannel mc) {
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.mute.success"),
                user.getName(), src.getName())));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("standard.reason"), rs)));

        if (user.isOnline()) {
            user.getPlayer().get().sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("mute.playernotify")));
            user.getPlayer().get().sendMessage(Text.of(TextColors.RED, MessageFormat.format(Util.messageBundle.getString("command.reason"), rs)));
        }

        return new MuteData(ua, rs);
    }
}

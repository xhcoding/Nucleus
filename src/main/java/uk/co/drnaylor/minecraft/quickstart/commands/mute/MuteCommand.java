/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.mute;

import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
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
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.MuteData;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.TimespanParser;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandPermissionHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.PermissionInformation;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.SuggestedLevel;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Mutes or unmutes a player.
 *
 * Command Usage: /mute user [time] [reason]
 * Permission: quickstart.mute.base
 * Notify: quickstart.mute.notify
 */
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@Modules(PluginModule.MUTES)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({ "mute", "unmute" })
public class MuteCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader userConfigLoader;

    private final String notifyPermission = CommandPermissionHandler.PERMISSIONS_PREFIX + "mute.notify";

    private String playerArgument = "Player";
    private String timespanArgument = "Time";
    private String reason = "Reason";

    @Override
    public Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put(notifyPermission, new PermissionInformation(Util.getMessageWithFormat("permission.mute.notify"), SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Mutes or unmutes a player")).executor(this).arguments(
                GenericArguments.onlyOne(new UserParser(Text.of(playerArgument))),
                GenericArguments.onlyOne(GenericArguments.optionalWeak(new TimespanParser(Text.of(timespanArgument)))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason))))
        ).build();
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
            throw new CommandException(Text.of(TextColors.RED, Util.getMessageWithFormat("command.file.load")), e);
        }

        Optional<Long> time = args.<Long>getOne(timespanArgument);
        Optional<MuteData> omd = uc.getMuteData();
        Optional<String> reas = args.<String>getOne(reason);

        // No time, no reason, but is muted, unmute
        if (omd.isPresent() && !time.isPresent() && !reas.isPresent()) {
            // Unmute.
            uc.removeMuteData();
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.unmute.success"), user.getName(), src.getName())));
            return CommandResult.success();
        }

        // Do we have a time?
        String rs = reas.orElse(Util.getMessageWithFormat("command.mute.defaultreason"));
        UUID ua = Util.consoleFakeUUID;
        if (src instanceof Player) {
            ua = ((Player) src).getUniqueId();
        }

        MutableMessageChannel mc = MessageChannel.permission(notifyPermission).asMutable();
        mc.addMember(src);
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
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.mute.success.time"),
                user.getName(), src.getName(), ts)));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("standard.reason"), rs)));

        user.getPlayer().get().sendMessage(Text.of(TextColors.RED, MessageFormat.format(Util.getMessageWithFormat("mute.playernotify.time"), ts)));
        user.getPlayer().get().sendMessage(Text.of(TextColors.RED, MessageFormat.format(Util.getMessageWithFormat("command.reason"), rs)));
        return new MuteData(ua, rs, Instant.now().plus(time, ChronoUnit.SECONDS));
    }

    private MuteData offlineTimedMute(CommandSource src, User user, long time, String rs, UUID ua, MessageChannel mc) {
        String ts = Util.getTimeStringFromSeconds(time);
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.mute.success.time"),
                user.getName(), src.getName(), ts)));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("standard.reason"), rs)));
        return new MuteData(ua, rs, Duration.of(time, ChronoUnit.SECONDS));
    }

    private MuteData permMute(CommandSource src, User user, String rs, UUID ua, MessageChannel mc) {
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.mute.success"),
                user.getName(), src.getName())));
        mc.send(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("standard.reason"), rs)));

        if (user.isOnline()) {
            user.getPlayer().get().sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("mute.playernotify")));
            user.getPlayer().get().sendMessage(Text.of(TextColors.RED, MessageFormat.format(Util.getMessageWithFormat("command.reason"), rs)));
        }

        return new MuteData(ua, rs);
    }
}

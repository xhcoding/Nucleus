/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.MuteData;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanParser;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
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

import java.io.IOException;
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
 * Command Usage: /mute user [time] [reason] Permission: quickstart.mute.base
 * Notify: quickstart.mute.notify
 */
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"mute", "unmute"})
public class MuteCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader userConfigLoader;
    @Inject private MuteHandler handler;

    private final String notifyPermission = PermissionRegistry.PERMISSIONS_PREFIX + "mute.notify";

    private String playerArgument = "player";
    private String timespanArgument = "time";
    private String reason = "reason";

    @Override
    public Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put(notifyPermission, new PermissionInformation(Util.getMessageWithFormat("permission.mute.notify"), SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Mutes or unmutes a player")).executor(this)
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of(playerArgument))),
                        GenericArguments.onlyOne(GenericArguments.optionalWeak(new TimespanParser(Text.of(timespanArgument)))),
                        GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason)))))
                .build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {

        // Get the user.
        User user = args.<User>getOne(playerArgument).get();
        NucleusUser uc;
        try {
            uc = userConfigLoader.getUser(user);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            throw new CommandException(Util.getTextMessageWithFormat("command.file.load"), e);
        }

        Optional<Long> time = args.getOne(timespanArgument);
        Optional<MuteData> omd = handler.getPlayerMuteData(user);
        Optional<String> reas = args.getOne(reason);

        // No time, no reason, but is muted, unmute
        if (omd.isPresent() && !time.isPresent() && !reas.isPresent()) {
            // Unmute.
            handler.unmutePlayer(user);
            src.sendMessage(Util.getTextMessageWithFormat("command.unmute.success", user.getName(), src.getName()));
            return CommandResult.success();
        }

        // Do we have a time?
        String rs = reas.orElse(Util.getMessageWithFormat("command.mute.defaultreason"));
        UUID ua = Util.consoleFakeUUID;
        if (src instanceof Player) {
            ua = ((Player) src).getUniqueId();
        }

        MuteData data;
        if (time.isPresent()) {
            data = new MuteData(ua, rs, Duration.ofSeconds(time.get()));
        } else {
            data = new MuteData(ua, rs);
        }

        if (handler.mutePlayer(user, data)) {
            // Success.
            MutableMessageChannel mc = MessageChannel.permission(notifyPermission).asMutable();
            mc.addMember(src);

            if (time.isPresent()) {
                timedMute(src, user, data, time.get(), mc);
            } else {
                permMute(src, user, data, mc);
            }

            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.mute.fail", user.getName()));
        return CommandResult.empty();
    }

    private void timedMute(CommandSource src, User user, MuteData data, long time, MessageChannel mc) {
        String ts = Util.getTimeStringFromSeconds(time);
        mc.send(Util.getTextMessageWithFormat("command.mute.success.time", user.getName(), src.getName(), ts));
        mc.send(Util.getTextMessageWithFormat("standard.reason", data.getReason()));

        if (user.isOnline()) {
            user.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("mute.playernotify.time", ts));
            user.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("command.reason", data.getReason()));
        }
    }

    private void permMute(CommandSource src, User user, MuteData data, MessageChannel mc) {
        mc.send(Util.getTextMessageWithFormat("command.mute.success.norm", user.getName(), src.getName()));
        mc.send(Util.getTextMessageWithFormat("standard.reason", data.getReason()));

        if (user.isOnline()) {
            user.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("mute.playernotify"));
            user.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("command.reason", data.getReason()));
        }
    }
}

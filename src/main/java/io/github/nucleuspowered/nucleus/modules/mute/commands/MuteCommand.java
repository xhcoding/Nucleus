/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ResortUserArgumentParser;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

@NonnullByDefault
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoModifiers
@RegisterCommand({"mute", "unmute"})
@EssentialsEquivalent({"mute", "silence"})
public class MuteCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private final MuteConfigAdapter mca;
    private final MuteHandler handler;

    private boolean requireUnmutePermission = false;
    private long maxMute = Long.MAX_VALUE;

    private final static String mutedChatPermission = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(MuteCommand.class)
            .getPermissionWithSuffix("seemutedchat");

    @Inject
    public MuteCommand(MuteConfigAdapter mca, MuteHandler handler) {
        this.mca = mca;
        this.handler = handler;
    }

    public static String getMutedChatPermission() {
        return mutedChatPermission;
    }

    private final String playerArgument = "subject";
    private final String timespanArgument = "time";
    private final String reason = "reason";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("unmute", PermissionInformation.getWithTranslation("permission.mute.unmute", SuggestedLevel.MOD));
        m.put("exempt.length", PermissionInformation.getWithTranslation("permission.mute.exempt.length", SuggestedLevel.ADMIN));
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.mute.exempt.target", SuggestedLevel.MOD));
        m.put("notify", PermissionInformation.getWithTranslation("permission.mute.notify", SuggestedLevel.MOD));
        m.put("seemutedchat", PermissionInformation.getWithTranslation("permission.mute.seemutedchat", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new ResortUserArgumentParser(Text.of(playerArgument))),
            GenericArguments.onlyOne(GenericArguments.optionalWeak(new TimespanArgument(Text.of(timespanArgument)))),
            GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason))))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {

        // Get the user.
        User user = args.<User>getOne(playerArgument).get();

        Optional<Long> time = args.getOne(timespanArgument);
        Optional<MuteData> omd = handler.getPlayerMuteData(user);
        Optional<String> reas = args.getOne(reason);

        if (permissions.testSuffix(user, "exempt.target", src, false)) {
            throw ReturnMessageException.fromKey("command.mute.exempt", user.getName());
        }

        // No time, no reason, but is muted, unmute
        if (omd.isPresent() && !time.isPresent() && !reas.isPresent()) {
            if (!this.requireUnmutePermission || this.permissions.testSuffix(src, "unmute")) {
                // Unmute.
                this.handler.unmutePlayer(user, Cause.of(NamedCause.source(src)), false);
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.unmute.success", user.getName(), src.getName()));
                return CommandResult.success();
            }

            throw ReturnMessageException.fromKey("command.unmute.perm");
        }

        // Do we have a reason?
        String rs = reas.orElse(plugin.getMessageProvider().getMessageWithFormat("command.mute.defaultreason"));
        UUID ua = Util.consoleFakeUUID;
        if (src instanceof Player) {
            ua = ((Player) src).getUniqueId();
        }

        if (this.maxMute > 0 && time.orElse(Long.MAX_VALUE) > this.maxMute && !permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.mute.length.toolong",
                    Util.getTimeStringFromSeconds(mca.getNodeOrDefault().getMaximumMuteLength()));
        }

        MuteData data;
        if (time.isPresent()) {
            if (!user.isOnline()) {
                data = new MuteData(ua, rs, Duration.ofSeconds(time.get()));
            } else {
                data = new MuteData(ua, rs, Instant.now().plus(time.get(), ChronoUnit.SECONDS));
            }
        } else {
            data = new MuteData(ua, rs);
        }

        if (handler.mutePlayer(user, data)) {
            // Success.
            MutableMessageChannel mc = new PermissionMessageChannel(permissions.getPermissionWithSuffix("notify")).asMutable();
            mc.addMember(src);

            if (time.isPresent()) {
                timedMute(src, user, data, time.get(), mc);
            } else {
                permMute(src, user, data, mc);
            }

            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.mute.fail", user.getName()));
        return CommandResult.empty();
    }

    private void timedMute(CommandSource src, User user, MuteData data, long time, MessageChannel mc) {
        String ts = Util.getTimeStringFromSeconds(time);
        mc.send(plugin.getMessageProvider().getTextMessageWithFormat("command.mute.success.time", user.getName(), src.getName(), ts));
        mc.send(plugin.getMessageProvider().getTextMessageWithFormat("standard.reason", data.getReason()));

        if (user.isOnline()) {
            user.getPlayer().get().sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("mute.playernotify.time", ts));
            user.getPlayer().get().sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.reason", data.getReason()));
        }
    }

    private void permMute(CommandSource src, User user, MuteData data, MessageChannel mc) {
        mc.send(plugin.getMessageProvider().getTextMessageWithFormat("command.mute.success.norm", user.getName(), src.getName()));
        mc.send(plugin.getMessageProvider().getTextMessageWithFormat("standard.reason", data.getReason()));

        if (user.isOnline()) {
            user.getPlayer().get().sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("mute.playernotify.standard"));
            user.getPlayer().get().sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.reason", data.getReason()));
        }
    }

    @Override
    public void onReload() {
        this.requireUnmutePermission = this.mca.getNodeOrDefault().isRequireUnmutePermission();
        this.maxMute = this.mca.getNodeOrDefault().getMaximumMuteLength();
    }
}

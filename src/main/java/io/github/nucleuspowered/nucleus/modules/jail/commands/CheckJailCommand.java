/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;

@Permissions(prefix = "jail", suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"checkjail"})
public class CheckJailCommand extends AbstractCommand<CommandSource> {

    private final String playerKey = "user/UUID";
    @Inject private JailHandler handler;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.firstParsing(
                GenericArguments.user(Text.of(playerKey)),
                new UUIDArgument<>(Text.of(playerKey), u -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(u))
            )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();
        Optional<JailData> jail = handler.getPlayerJailDataInternal(user);

        if (!jail.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkjail.nojail", user.getName()));
            return CommandResult.success();
        }

        JailData md = jail.get();
        String name;
        if (md.getJailerInternal().equals(Util.consoleFakeUUID)) {
            name = Sponge.getServer().getConsole().getName();
        } else {
            name = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(md.getJailerInternal())
                    .map(User::getName).orElseGet(() -> plugin.getMessageProvider().getMessageWithFormat("standard.unknown"));
        }

        if (md.getRemainingTime().isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkjail.jailedfor", user.getName(), md.getJailName(),
                    name, Util.getTimeStringFromSeconds(md.getRemainingTime().get().getSeconds())));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkjail.jailedperm", user.getName(), md.getJailName(),
                    name));
        }

        if (md.getCreationTime() > 0) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkjail.created",
                    Util.FULL_TIME_FORMATTER.withLocale(src.getLocale()).format(Instant.ofEpochSecond(md.getCreationTime()))));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkjail.created",
                    plugin.getMessageProvider().getMessageWithFormat("standard.unknown")));
        }

        src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(plugin.getMessageProvider().getMessageWithFormat("standard.reason"), md.getReason())));
        return CommandResult.success();
    }
}

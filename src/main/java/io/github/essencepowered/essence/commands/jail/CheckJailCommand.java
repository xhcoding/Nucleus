/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.jail;

import com.google.inject.Inject;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.api.data.JailData;
import io.github.essencepowered.essence.argumentparsers.UserParser;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.JailHandler;
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

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Permissions(root = "jail", suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@Modules(PluginModule.JAILS)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"checkjail"})
public class CheckJailCommand extends CommandBase<CommandSource> {

    private final String playerKey = "playerKey";
    @Inject private JailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(GenericArguments.onlyOne(new UserParser(Text.of(playerKey)))).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();
        Optional<JailData> jail = handler.getPlayerJailData(user);

        if (!jail.isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.checkjail.nojail", user.getName()));
            return CommandResult.success();
        }

        JailData md = jail.get();
        String name;
        if (md.getJailer().equals(Util.consoleFakeUUID)) {
            name = Sponge.getServer().getConsole().getName();
        } else {
            Optional<User> ou = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(md.getJailer());
            name = ou.isPresent() ? ou.get().getName() : Util.getMessageWithFormat("standard.unknown");
        }

        String time = "";
        String forString = "";
        if (md.getEndTimestamp().isPresent()) {
            time = Util.getTimeStringFromSeconds(Instant.now().until(md.getEndTimestamp().get(), ChronoUnit.SECONDS));
            forString = " " + Util.getMessageWithFormat("standard.for") + " ";
        } else if (md.getTimeFromNextLogin().isPresent()) {
            time = Util.getTimeStringFromSeconds(md.getTimeFromNextLogin().get().getSeconds());
            forString = " " + Util.getMessageWithFormat("standard.for") + " ";
        }

        src.sendMessage(Text.of(TextColors.GREEN,
                MessageFormat.format(Util.getMessageWithFormat("command.checkjail.jailed"), user.getName(), md.getJailName(), name, forString, time)));
        src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("standard.reason"), md.getReason())));
        return CommandResult.success();
    }
}

/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.MuteData;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Checks the mute status of a player.
 *
 * Command Usage: /checkmute user
 * Permission: quickstart.checkmute.base
 */
@Permissions(includeMod = true)
@RunAsync
@Modules(PluginModule.MUTES)
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class CheckMuteCommand extends CommandBase {

    @Inject private UserConfigLoader userConfigLoader;
    private final String playerArgument = "Player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Checks the mute status of that player")).executor(this).arguments(
                GenericArguments.onlyOne(new UserParser(Text.of(playerArgument)))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "checkmute" };
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

        Optional<MuteData> omd = uc.getMuteData();
        if (!omd.isPresent()) {
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.checkmute.none"), user.getName())));
            return CommandResult.success();
        }

        // Muted, get information.
        MuteData md = omd.get();
        String name;
        if (md.getMuter().equals(Util.consoleFakeUUID)) {
            name = Sponge.getServer().getConsole().getName();
        } else {
            Optional<User> ou = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(md.getMuter());
            name = ou.isPresent() ? ou.get().getName() : Util.getMessageWithFormat("standard.unknown");
        }

        String time = "";
        String forString = "";
        if (md.getEndTimestamp().isPresent()) {
            time = Util.getTimeStringFromSeconds(Instant.now().until(md.getEndTimestamp().get(), ChronoUnit.SECONDS));
            forString = Util.getMessageWithFormat("standard.for") + " ";
        } else if (md.getTimeFromNextLogin().isPresent()) {
            time = Util.getTimeStringFromSeconds(md.getTimeFromNextLogin().get().getSeconds());
            forString = Util.getMessageWithFormat("standard.for") + " ";
        }

        src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.checkmute.mute"), user.getName(), name, forString, time)));
        src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("standard.reason"), md.getReason())));
        return CommandResult.success();
    }
}

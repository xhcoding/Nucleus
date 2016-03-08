/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.mail;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.services.MailHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * Permission - "quickstart.mail.send.use"
 */
@Permissions(root = "mail", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand(value = {"send", "s"}, subcommandOf = MailCommand.class)
public class SendMailCommand extends CommandBase<CommandSource> {

    @Inject private MailHandler handler;
    @Inject private PermissionRegistry permissionRegistry;

    private final String player = "player";
    private final String message = "message";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of(player))),
                GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = args.<User>getOne(player).orElseThrow(() -> new CommandException(Util.getTextMessageWithFormat("args.user.none")));
        Optional<CommandPermissionHandler> oservice = permissionRegistry.getService(MailCommand.class);

        // Only send mails to players that can read them.
        if (oservice.isPresent() && oservice.get().testBase(pl)) {
            src.sendMessage(Util.getTextMessageWithFormat("command.mail.send.error", pl.getName()));
            return CommandResult.empty();
        }

        // Send the message.
        String m = args.<String>getOne(message).orElseThrow(() -> new CommandException(Util.getTextMessageWithFormat("args.message.none")));
        if (src instanceof User) {
            handler.sendMail((User) src, pl, m);
        } else {
            handler.sendMailFromConsole(pl, m);
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.mail.send", pl.getName()));
        return CommandResult.success();
    }
}

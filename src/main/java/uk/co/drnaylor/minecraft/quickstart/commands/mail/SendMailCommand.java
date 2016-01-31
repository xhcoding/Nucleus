package uk.co.drnaylor.minecraft.quickstart.commands.mail;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;
import uk.co.drnaylor.minecraft.quickstart.internal.handlers.MailHandler;

/**
 * Permission - "quickstart.mail.send.use"
 */
@Permissions(root = "mail")
@RunAsync
public class SendMailCommand extends CommandBase {
    @Inject private MailHandler handler;
    private final String player = "player";
    private final String message = "message";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .arguments(
                        GenericArguments.onlyOne(new UserParser(Text.of(player))),
                        GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))
                )
                .build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "send", "s" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = args.<User>getOne(player).get();
        if (!pl.hasPermission(QuickStart.PERMISSIONS_ADMIN) && !pl.hasPermission(QuickStart.PERMISSIONS_PREFIX + "mail.base")) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.mail.send.error", pl.getName())));
            return CommandResult.empty();
        }

        // Send the message.
        String m = args.<String>getOne(message).get();
        if (src instanceof User) {
            handler.sendMail((User)src, pl, m);
        } else {
            handler.sendMailFromConsole(pl, m);
        }

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.mail.send", pl.getName())));
        return CommandResult.success();
    }
}

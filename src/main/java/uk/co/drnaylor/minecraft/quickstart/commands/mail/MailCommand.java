/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.mail;

import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailFilter;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.MailFilterParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.MailHandler;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Permissions(includeUser = true)
@RunAsync
@Modules(PluginModule.MAILS)
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class MailCommand extends CommandBase<Player> {
    @Inject private MailHandler handler;
    private final String filters = "filters";
    @Inject private Game game;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).children(this.createChildCommands(ClearMailCommand.class, SendMailCommand.class))
                .arguments(
                        GenericArguments.optional(GenericArguments.allOf(new MailFilterParser(Text.of(filters), handler)))
                ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "mail" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Collection<MailFilter> lmf = args.<MailFilter>getAll(filters);
        List<MailData> lmd;
        if (!lmf.isEmpty()) {
            lmd = handler.getMail(src, lmf.toArray(new MailFilter[lmf.size()]));
        } else {
            lmd = handler.getMail(src);
        }

        if (lmd.isEmpty()) {
            src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat(!lmf.isEmpty() ? "command.mail.none.filter" : "command.mail.none")));
            return CommandResult.success();
        }

        List<Text> mails = lmd.stream().sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .map(this::createMessage)
                .collect(Collectors.toList());

        // Paginate the mail.
        PaginationService ps = game.getServiceManager().provideUnchecked(PaginationService.class);
        ps.builder().paddingString("-").title(Text.of(TextColors.YELLOW, Util.getMessageWithFormat(lmf.isEmpty() ? "mail.title" : "mail.title.filter")))
                .header(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("mail.header"))).contents(mails)
                .sendTo(src);

        return CommandResult.success();
    }

    private Text createMessage(final MailData md) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault());
        return Text.builder().append(Text.builder(Util.getNameFromUUID(md.getUuid()))
                .color(TextColors.GREEN)
                .style(TextStyles.UNDERLINE)
                .onHover(TextActions.showText(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.hover"))))
                .onClick(TextActions.executeCallback(src -> {
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.date") + " ", TextColors.WHITE, dtf.format(md.getDate())));
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.sender") + " ", TextColors.WHITE, Util.getNameFromUUID(md.getUuid())));
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.message")));
                    src.sendMessage(Text.of(TextColors.WHITE, md.getMessage()));
                })).build())
                .append(Text.of(": " + md.getMessage())).build();
    }
}

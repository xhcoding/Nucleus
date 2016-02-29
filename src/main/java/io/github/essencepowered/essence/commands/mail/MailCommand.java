/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.mail;

import com.google.inject.Inject;
import io.github.essencepowered.essence.NameUtil;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.api.data.mail.MailData;
import io.github.essencepowered.essence.api.data.mail.MailFilter;
import io.github.essencepowered.essence.argumentparsers.MailFilterParser;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.MailHandler;
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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@Modules(PluginModule.MAILS)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand("mail")
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
        return Text.builder().append(Text.builder(NameUtil.getNameFromUUID(md.getUuid()))
                .color(TextColors.GREEN)
                .style(TextStyles.UNDERLINE)
                .onHover(TextActions.showText(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.hover"))))
                .onClick(TextActions.executeCallback(src -> {
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.date") + " ", TextColors.WHITE, dtf.format(md.getDate())));
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.sender") + " ", TextColors.WHITE, NameUtil.getNameFromUUID(md.getUuid())));
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.mail.message")));
                    src.sendMessage(Text.of(TextColors.WHITE, md.getMessage()));
                })).build())
                .append(Text.of(": " + md.getMessage())).build();
    }
}

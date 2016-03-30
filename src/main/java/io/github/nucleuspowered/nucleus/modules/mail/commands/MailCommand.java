/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.mail.MailData;
import io.github.nucleuspowered.nucleus.api.data.mail.MailFilter;
import io.github.nucleuspowered.nucleus.argumentparsers.MailFilterParser;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
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
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand("mail")
public class MailCommand extends CommandBase<Player> {

    @Inject private MailHandler handler;
    @Inject private Game game;
    private final String filters = "filters";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] { GenericArguments.optional(GenericArguments.allOf(new MailFilterParser(Text.of(filters), handler))) };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Collection<MailFilter> lmf = args.getAll(filters);
        List<MailData> lmd;
        if (!lmf.isEmpty()) {
            lmd = handler.getMail(src, lmf.toArray(new MailFilter[lmf.size()]));
        } else {
            lmd = handler.getMail(src);
        }

        if (lmd.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat(!lmf.isEmpty() ? "command.mail.none.filter" : "command.mail.none.normal"));
            return CommandResult.success();
        }

        List<Text> mails = lmd.stream().sorted((a, b) -> a.getDate().compareTo(b.getDate())).map(this::createMessage).collect(Collectors.toList());

        // Paginate the mail.
        PaginationService ps = game.getServiceManager().provideUnchecked(PaginationService.class);
        ps.builder().padding(Text.of(TextColors.GREEN, "-")).title(Util.getTextMessageWithFormat(lmf.isEmpty() ? "mail.title.nofilter" : "mail.title.filter"))
                .header(Util.getTextMessageWithFormat("mail.header")).contents(mails).sendTo(src);

        return CommandResult.success();
    }

    private Text createMessage(final MailData md) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault());
        return Text.builder()
                .append(Text.builder(NameUtil.getNameFromUUID(md.getUuid())).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                        .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.mail.hover")))
                        .onClick(TextActions.executeCallback(src -> {
                            src.sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.mail.date"))
                                    .append(Text.of(" ", TextColors.WHITE, dtf.format(md.getDate()))).build());
                            src.sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.mail.sender"))
                                    .append(Text.of(" ", TextColors.WHITE, NameUtil.getNameFromUUID(md.getUuid()))).build());
                            src.sendMessage(Util.getTextMessageWithFormat("command.mail.message"));
                            src.sendMessage(Text.of(TextColors.WHITE, md.getMessage()));
                        })).build())
                .append(Text.of(": " + md.getMessage())).build();
    }
}

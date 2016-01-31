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
import uk.co.drnaylor.minecraft.quickstart.internal.handlers.MailHandler;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Permissions
@RunAsync
@Modules(PluginModule.MAILS)
@NoWarmup
@NoCooldown
@NoCost
public class MailCommand extends CommandBase<Player> {
    @Inject private MailHandler handler;
    private final String filters = "filters";
    @Inject private Game game;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).children(this.createChildCommands(ClearMailCommand.class, SendMailCommand.class))
                .arguments(
                        GenericArguments.optional(GenericArguments.onlyOne(new MailFilterParser(Text.of(filters), handler)))
                ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "mail" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Optional<List<MailFilter>> olmf = args.getOne(filters);
        List<MailData> lmd;
        if (olmf.isPresent()) {
            List<MailFilter> lmf = olmf.get();
            lmd = handler.getMail(src, olmf.get().toArray(new MailFilter[lmf.size()]));
        } else {
            lmd = handler.getMail(src);
        }

        if (lmd.isEmpty()) {
            src.sendMessage(Text.of(TextColors.YELLOW, Util.messageBundle.getString(olmf.isPresent() ? "command.mail.none.filter" : "command.mail.none")));
            return CommandResult.success();
        }

        List<Text> mails = lmd.stream().sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .map(this::createMessage)
                .collect(Collectors.toList());

        // Paginate the mail.
        PaginationService ps = game.getServiceManager().provideUnchecked(PaginationService.class);
        ps.builder().paddingString("-").title(Text.of(TextColors.YELLOW, Util.messageBundle.getString("mail.title")))
                .header(Text.of(TextColors.YELLOW, Util.messageBundle.getString("mail.header"))).contents(mails)
                .sendTo(src);

        return CommandResult.success();
    }

    private Text createMessage(final MailData md) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MMM-dd").withZone(ZoneId.systemDefault());
        return Text.builder().append(Text.builder(Util.getNameFromUUID(md.getUuid()))
                .color(TextColors.GREEN)
                .style(TextStyles.UNDERLINE)
                .onHover(TextActions.showText(Text.of(TextColors.YELLOW, Util.messageBundle.getString("command.mail.hover"))))
                .onClick(TextActions.executeCallback(src -> {
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.messageBundle.getString("command.mail.date") + " ", TextColors.WHITE, dtf.format(md.getDate())));
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.messageBundle.getString("command.mail.sender") + " ", TextColors.WHITE, Util.getNameFromUUID(md.getUuid())));
                    src.sendMessage(Text.of(TextColors.YELLOW, Util.messageBundle.getString("command.mail.message")));
                    src.sendMessage(Text.of(TextColors.WHITE, md.getMessage()));
                })).build())
                .append(Text.of(": " + md.getMessage())).build();
    }
}

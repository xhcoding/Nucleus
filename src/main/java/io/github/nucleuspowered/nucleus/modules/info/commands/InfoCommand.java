/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.commands;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.InfoArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.info.handlers.InfoHandler;
import io.github.nucleuspowered.nucleus.modules.info.handlers.InfoHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"info", "einfo"})
public class InfoCommand extends CommandBase<CommandSource> {

    @Inject private InfoHandler infoHandler;
    @Inject private ChatUtil chatUtil;

    private final String key = "section";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(new InfoArgument(Text.of(key), infoHandler))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<InfoArgument.Result> oir = args.getOne(key);
        if (oir.isPresent()) {
            // Get the list.
            List<String> info = oir.get().text;
            Optional<String> os = getTitle(info);
            String title;
            if (os.isPresent()) {
                title = Util.getMessageWithFormat("command.info.title.section", os.get());

                // Just in case the list is immutable.
                info = Lists.newArrayList(info);
                info.remove(0);

                // Remove blank lines.
                Iterator<String> i = info.iterator();
                while (i.hasNext()) {
                    String n = i.next();
                    if (n.isEmpty() || n.matches("^\\s+$")) {
                        i.remove();
                    } else {
                        break;
                    }
                }
            } else {
                title = Util.getMessageWithFormat("command.info.title.section", oir.get().name);
            }

            InfoHelper.sendInfo(info, src, chatUtil, title);
            return CommandResult.success();
        }

        // Create a list of pages to load.
        Set<String> sections = infoHandler.getInfoSections();
        if (sections.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.info.none"));
            return CommandResult.empty();
        }

        // Create the text.
        List<Text> s = Lists.newArrayList();
        sections.forEach(x -> {
            Text.Builder tb = Text.builder().append(Text.builder(x)
                    .color(TextColors.GREEN).style(TextStyles.ITALIC)
                    .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.info.hover", x)))
                    .onClick(TextActions.runCommand("/info " + x)).build());

            // If there is a title, then add it.
            getTitle(infoHandler.getSection(x).get()).ifPresent(sub ->
                    tb.append(Text.of(TextColors.GOLD, " - ")).append(chatUtil.getServerMessageFromTemplate(sub, src, true)));

            s.add(tb.build());
        });

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        PaginationList.Builder pb = ps.builder().contents()
                .header(Util.getTextMessageWithFormat("command.info.header.default"))
                .title(Util.getTextMessageWithFormat("command.info.title.default"))
                .contents(s.stream().sorted((a, b) -> a.toPlain().compareTo(b.toPlain())).collect(Collectors.toList()))
                .padding(Text.of(TextColors.GOLD, "-"));

        if (src instanceof ConsoleSource) {
            pb.linesPerPage(-1);
        }

        pb.sendTo(src);
        return CommandResult.success();
    }

    private Optional<String> getTitle(List<String> info) {
        if (!info.isEmpty()) {
            String sec1 = info.get(0);
            if (sec1.startsWith("#")) {
                // Get rid of the # and spaces, then limit to 50 characters.
                sec1 = sec1.replaceFirst("#\\s*", "");
                if (sec1.length() > 50) {
                    sec1 = sec1.substring(0, 50);
                }

                return Optional.of(sec1);
            }
        }

        return Optional.empty();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoCooldown
@NoCost
@NoWarmup
@RunAsync
@RegisterCommand("jails")
@Permissions(root = "jail", alias = "list", suggestedLevel = SuggestedLevel.MOD)
public class JailsCommand extends CommandBase<CommandSource> {

    @Inject private JailHandler handler;

    @Override
    public CommandElement[] getArguments() {
        return super.getArguments();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);

        Map<String, WarpLocation> mjs = handler.getJails();
        if (mjs.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.jails.nojails"));
            return CommandResult.empty();
        }

        List<Text> lt = mjs.entrySet().stream()
                .map(x -> Text.builder(x.getKey().toLowerCase()).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                        .onClick(TextActions.runCommand("/jails info " + x.getKey().toLowerCase()))
                        .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.jails.jailprompt", x.getKey().toLowerCase()))).build())
                .collect(Collectors.toList());

        ps.builder().title(Util.getTextMessageWithFormat("command.jails.list.header")).padding(Text.of(TextColors.GREEN, "-")).contents(lt).sendTo(src);
        return CommandResult.success();
    }
}

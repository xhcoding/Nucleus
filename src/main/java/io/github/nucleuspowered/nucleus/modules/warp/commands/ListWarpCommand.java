/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lists all the warps that a player can access.
 *
 * Command Usage: /warp list Permission: quickstart.warp.list.base
 */
@Permissions(root = "warp", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand(value = {"list"}, subcommandOf = WarpCommand.class)
public class ListWarpCommand extends CommandBase<CommandSource> {

    private NucleusWarpService service;
    @Inject private WarpConfigAdapter adapter;

    @Override
    public CommandElement[] getArguments() {
        return super.getArguments();
    }

    @Override
    public String[] getAliases() {
        return new String[] {"list"};
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        if (service == null) {
            service = Sponge.getServiceManager().provideUnchecked(NucleusWarpService.class);
        }

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);

        // Get the warp list.
        Set<String> ws = service.getWarpNames();
        List<Text> lt = ws.stream().filter(s -> canView(src, s.toLowerCase())).map(s -> {
            if (service.getWarp(s).isPresent()) {
                return Text.builder(s).color(TextColors.GREEN).style(TextStyles.UNDERLINE).onClick(TextActions.runCommand("/warp " + s))
                        .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.warps.warpprompt", s))).build();
            } else {
                return Text.builder(s).color(TextColors.RED).onHover(TextActions.showText(Util.getTextMessageWithFormat("command.warps.unavailable")))
                        .build();
            }
        }).collect(Collectors.toList());

        ps.builder().title(Util.getTextMessageWithFormat("command.warps.list.header")).padding(Text.of(TextColors.GREEN, "-")).contents(lt)
                .sendTo(src);
        return CommandResult.success();
    }

    private boolean canView(CommandSource src, String warp) {
        return !adapter.getNodeOrDefault().isSeparatePermissions()
                || src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "warps." + warp.toLowerCase());
    }
}

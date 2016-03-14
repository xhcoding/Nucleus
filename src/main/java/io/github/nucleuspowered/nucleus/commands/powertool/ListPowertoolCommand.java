/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.powertool;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lists the powertools associated with the items.
 *
 * Permission: nucleus.powertool.base (uses the base permission)
 */
@Permissions(alias = "powertool")
@RunAsync
@NoCooldown
@NoWarmup
@NoCost
@RegisterCommand(value = {"list", "ls"}, subcommandOf = PowertoolCommand.class)
public class ListPowertoolCommand extends CommandBase<Player> {

    @Inject private UserConfigLoader loader;
    private PaginationService paginationService = null;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (paginationService == null) {
            paginationService = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        }

        InternalNucleusUser inu = loader.getUser(src);

        boolean toggle = inu.isPowertoolToggled();
        Map<String, List<String>> powertools = inu.getPowertools();

        if (powertools.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.powertool.list.none"));
            return CommandResult.success();
        }

        // Generate powertools.
        List<Text> mesl = powertools.entrySet().stream().sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                .map(k -> from(inu, src, k.getKey(), k.getValue())).collect(Collectors.toList());

        // Paginate the tools.
        paginationService.builder().title(Util.getTextMessageWithFormat("command.powertool.list.header", toggle ? "&aenabled" : "&cdisabled"))
                .padding(Text.of(TextColors.YELLOW, "-")).contents(mesl).sendTo(src);

        return CommandResult.success();
    }

    private Text from(final InternalNucleusUser inu, Player src, String powertool, List<String> commands) {
        Optional<ItemType> oit = Sponge.getRegistry().getType(ItemType.class, powertool);

        // Create the click actions.
        ClickAction viewAction = TextActions.executeCallback(pl -> {
            paginationService.builder().title(Util.getTextMessageWithFormat("command.powertool.ind.header", powertool))
                .padding(Text.of(TextColors.GREEN, "-")).contents(
                    commands.stream().map(x -> Text.of(TextColors.YELLOW, x)).collect(Collectors.toList())
                ).sendTo(src);
        });

        ClickAction deleteAction = TextActions.executeCallback(pl -> {
            inu.clearPowertool(powertool);
            pl.sendMessage(Util.getTextMessageWithFormat("command.powertool.removed", powertool));
        });

        TextColor tc = oit.isPresent() ? TextColors.YELLOW : TextColors.GRAY;

        // id - [View] - [Delete]
        return Text.builder().append(Text.of(tc, powertool)).append(Text.of(" - ")).append(
                Text.builder(Util.getMessageWithFormat("standard.view")).color(TextColors.YELLOW).onClick(viewAction).build()
        ).append(Text.of(" - ")).append(
                Text.builder(Util.getMessageWithFormat("standard.delete")).color(TextColors.DARK_RED).onClick(deleteAction).build()
        ).build();
    }
}

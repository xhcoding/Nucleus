/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all the worlds.
 *
 * Command Usage: /world list Permission: plugin.world.list.base
 */
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"list", "ls"}, subcommandOf = WorldCommand.class)
public class ListWorldCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        List<String> worlds = Sponge.getServer().getWorlds().stream().filter(world -> world.getProperties().isEnabled()).map(World::getName)
                .collect(Collectors.toList());

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        ArrayList<Text> worldText = Lists.newArrayList();

        for (String name : worlds) {
            Text item = Text.builder(name).onClick(TextActions.runCommand("/world tp " + name))
                    .onHover(TextActions.showText(Text.of(TextColors.WHITE, "Teleport to world ", TextColors.GOLD, name))).color(TextColors.DARK_AQUA)
                    .style(TextStyles.UNDERLINE).build();

            worldText.add(item);
        }

        PaginationList.Builder paginationBuilder = paginationService.builder().contents(worldText).title(Text.of(TextColors.GREEN, "Showing Worlds"))
                .padding(Text.of(TextColors.GREEN, "-"));
        paginationBuilder.sendTo(src);

        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.WorldArchetype;

import java.util.List;
import java.util.stream.Collectors;

@NonnullByDefault
@NoModifiers
@RegisterCommand(value = {"presets", "listpresets"}, subcommandOf = WorldCommand.class)
@Permissions(prefix = "world", mainOverride = "create")
public class AvailablePresetsCommand extends AbstractCommand<CommandSource> {

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        // Get all the WorldArchetypes
        List<Text> types = Sponge.getRegistry().getAllOf(WorldArchetype.class).stream()
                .map(x -> mp.getTextMessageWithFormat("command.world.presets.item", x.getId(), x.getName()))
                .collect(Collectors.toList());

        Util.getPaginationBuilder(src).title(mp.getTextMessageWithTextFormat("command.world.presets.title"))
            .contents(types).sendTo(src);

        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.modules.world.WorldHelper;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.inject.Inject;
import java.util.Optional;

@NoCooldown
@NoCost
@NoWarmup
@Permissions(root = "world.border", alias = "gen")
@RegisterCommand(value = "cancelgen", subcommandOf = BorderCommand.class)
public class CancelChunkGenCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    private final String worldKey = "world";

    @Inject
    private WorldHelper worldHelper;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.world(Text.of(worldKey))))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<WorldProperties> owp = args.getOne(worldKey);
        WorldProperties wp;
        if (owp.isPresent()) {
            wp = owp.get();
        } else {
            if (src instanceof Locatable) {
                wp = ((Locatable) src).getWorld().getProperties();
            } else {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.setborder.noworld"));
                return CommandResult.empty();
            }
        }

        if (worldHelper.cancelPregenRunningForWorld(wp.getUniqueId())) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.cancelgen.cancelled", wp.getWorldName()));
            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.cancelgen.notask", wp.getWorldName()));
        return CommandResult.empty();
    }
}

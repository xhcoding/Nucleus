/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

/**
 * Loads worlds.
 *
 * Command Usage: /world load [name]
 * Permission: nucleus.world.load.base
 */
@Permissions(root = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"load"}, subcommandOf = WorldCommand.class)
public class LoadWorldCommand extends CommandBase<CommandSource> {

    private final String name = "name";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.world(Text.of(name))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        WorldProperties worldProperties = args.<WorldProperties>getOne(name).get();
        src.sendMessage(Util.getTextMessageWithFormat("command.world.load.begin", worldProperties.getWorldName()));
        if (Sponge.getServer().getUnloadedWorlds().stream().anyMatch(x -> x.getUniqueId().equals(worldProperties.getUniqueId()))) {
            Sponge.getGame().getServer().loadWorld(worldProperties);

            src.sendMessage(Util.getTextMessageWithFormat("command.world.load.success", worldProperties.getWorldName()));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.world.load.alreadyloaded", worldProperties.getWorldName()));
        return CommandResult.empty();
    }
}

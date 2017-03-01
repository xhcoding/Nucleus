/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.troubleshoot.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.stream.StreamSupport;

@Permissions(prefix = "troubleshoot")
@RegisterCommand(value = "chunkunload", subcommandOf = TroubleshootCommand.class)
public class ChunkUnloadCommand extends AbstractCommand<CommandSource> {

    private final String key = "world";

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(GenericArguments.world(Text.of(key)))
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Request all chunks to be unloaded.
        WorldProperties worldProperties =  this.getWorldFromUserOrArgs(src, key, args);
        World loaded = Sponge.getServer().getWorld(worldProperties.getUniqueId())
            .orElseThrow(() -> ReturnMessageException.fromKey("command.chunkunload.notloaded", worldProperties.getWorldName()));

        // Attempt to unload all chunks.
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.chunkunload.start", worldProperties.getWorldName()));
        loaded.save();
        long count = StreamSupport.stream(loaded.getLoadedChunks().spliterator(), false).filter(Chunk::unloadChunk).count();
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.chunkunload.done",
                String.valueOf(count), worldProperties.getWorldName()));

        return CommandResult.success();
    }
}

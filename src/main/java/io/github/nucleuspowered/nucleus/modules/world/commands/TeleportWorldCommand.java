/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

/**
 * Teleports you to the world specified.
 *
 * Command Usage: /world teleport [world] Permission:
 * plugin.world.teleport.base
 */
@Permissions(root = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"teleport", "tp"}, subcommandOf = WorldCommand.class)
public class TeleportWorldCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    private final String world = "world";
    private final String playerKey = "player";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.seq(GenericArguments.world(Text.of(world)),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey)))))};
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        Optional<Player> optPlayer = args.getOne(playerKey);
        WorldProperties worldProperties = args.<WorldProperties>getOne(world).get();

        if (!worldProperties.isEnabled()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.notenabled", worldProperties.getWorldName()));
            return CommandResult.success();
        }

        Player player;
        if (!optPlayer.isPresent()) {
            if (src instanceof Player) {
                player = (Player) src;
            } else {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.player", worldProperties.getWorldName()));
                return CommandResult.success();
            }
        } else {
            player = optPlayer.get();
        }

        World world = Sponge.getServer().getWorld(worldProperties.getUniqueId()).get();
        player.transferToWorld(world.getUniqueId(), world.getSpawnLocation().getPosition());
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.teleport.success", world.getName()));

        return CommandResult.success();
    }
}

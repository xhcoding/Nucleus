/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jump.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Permissions(suggestedLevel = SuggestedLevel.ADMIN)
@NonnullByDefault
@RegisterCommand("unstuck")
public class UnstuckCommand extends AbstractCommand<Player> {

    @Override
    protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the player location, find a safe location. Prevent players trying this to get out of sticky situations, height and width is 1.
        Location<World> location = Sponge.getGame().getTeleportHelper().getSafeLocation(src.getLocation(), 1, 1)
            .orElseThrow(() -> ReturnMessageException.fromKey("command.unstuck.nolocation"));
        if (location.getBlockPosition().equals(src.getLocation().getBlockPosition())) {
            throw ReturnMessageException.fromKey("command.unstuck.notneeded");
        }

        if (src.setLocation(location)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithTextFormat("command.unstuck.success"));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.unstuck.cancelled");
    }
}

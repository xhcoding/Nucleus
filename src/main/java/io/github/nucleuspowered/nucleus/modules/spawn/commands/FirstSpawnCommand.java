/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.back.handlers.BackHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("firstspawn")
public class FirstSpawnCommand extends CommandBase<Player> {

    @Inject(optional = true) private BackHandler backHandler;
    @Inject private GeneralDataStore data;

    @Override
    public CommandElement[] getArguments() {
        return super.getArguments();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {

        Optional<Transform<World>> olwr = data.getFirstSpawn();
        if (!olwr.isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.firstspawn.notset"));
            return CommandResult.empty();
        }

        Transform<World> currentLocation = src.getTransform();
        if (src.setLocationAndRotationSafely(olwr.get().getLocation(), olwr.get().getRotation())) {
            if (backHandler != null) {
                backHandler.setLastLocationInternal(src, currentLocation);
            }

            src.sendMessage(Util.getTextMessageWithFormat("command.firstspawn.success"));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.firstspawn.fail"));
        return CommandResult.empty();
    }
}

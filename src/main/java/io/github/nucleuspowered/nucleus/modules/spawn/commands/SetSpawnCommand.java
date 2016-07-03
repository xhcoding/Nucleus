/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;

import javax.inject.Inject;

@RegisterCommand({"setspawn"})
@Permissions
@NoWarmup
@NoCooldown
@NoCost
public class SetSpawnCommand extends CommandBase<Player> {

    @Inject
    private WorldDataManager wcl;

    @Override
    public CommandElement[] getArguments() {
        return super.getArguments();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Minecraft does not set the rotation of the player at the spawn point, so we'll do it for them!
        wcl.getWorld(src.getWorld().getUniqueId()).get().setSpawnRotation(src.getRotation());
        src.getWorld().getProperties().setSpawnPosition(src.getLocation().getBlockPosition());
        src.sendMessage(Util.getTextMessageWithFormat("command.setspawn.success", src.getWorld().getName()));
        return CommandResult.success();
    }
}

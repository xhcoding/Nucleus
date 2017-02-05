/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularWorldService;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodule.SpawnWorldDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import javax.inject.Inject;

@RegisterCommand({"setspawn"})
@Permissions
@NoWarmup
@NoCooldown
@NoCost
public class SetSpawnCommand extends AbstractCommand<Player> {

    @Inject
    private WorldDataManager wcl;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Minecraft does not set the rotation of the player at the spawn point, so we'll do it for them!
        ModularWorldService worldService = wcl.getWorld(src.getWorld().getUniqueId()).get();
        SpawnWorldDataModule m = worldService.get(SpawnWorldDataModule.class);
        m.setSpawnRotation(src.getRotation());
        worldService.set(m);

        src.getWorld().getProperties().setSpawnPosition(src.getLocation().getBlockPosition());
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.setspawn.success", src.getWorld().getName()));
        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.spawn;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;

@RegisterCommand({"setspawn"})
@Modules(PluginModule.SPAWN)
@Permissions
@NoWarmup
@NoCooldown
@NoCost
public class SetSpawnCommand extends CommandBase<Player> {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        src.getWorld().getProperties().setSpawnPosition(src.getLocation().getBlockPosition());
        src.sendMessage(Util.getTextMessageWithFormat("command.setspawn.success", src.getWorld().getName()));
        return CommandResult.success();
    }
}

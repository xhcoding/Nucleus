/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.spawn;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@RegisterCommand({ "setspawn" })
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
        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.setspawn.success", src.getWorld().getName())));
        return CommandResult.success();
    }
}

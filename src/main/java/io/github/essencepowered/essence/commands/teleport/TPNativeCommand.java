/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * This is a wrapper class for /minecraft:tp to map to "tpn", for those who want that.
 */
@NoCooldown
@NoCost
@NoWarmup
@Modules(PluginModule.TELEPORT)
@RegisterCommand({ "teleportnative", "tpnative", "tpn" })
public class TPNativeCommand extends CommandBase<CommandSource> {
    private final String a = "args";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.remainingJoinedStrings(Text.of(a))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Sponge.getCommandManager().process(src, String.format("minecraft:tp %s", args.<String>getOne(a).orElse("")));
        return CommandResult.success();
    }
}

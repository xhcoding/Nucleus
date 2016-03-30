/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
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
@NoPermissions
@RegisterCommand({ "teleportnative", "tpnative", "tpn" })
public class TPNativeCommand extends OldCommandBase<CommandSource> {
    private final String a = "args";

    @Override
    public CommandSpec createSpec() {
        return getSpecBuilderBase().arguments(GenericArguments.remainingJoinedStrings(Text.of(a))).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Sponge.getCommandManager().process(src, String.format("minecraft:tp %s", args.<String>getOne(a).orElse("")));
        return CommandResult.success();
    }
}

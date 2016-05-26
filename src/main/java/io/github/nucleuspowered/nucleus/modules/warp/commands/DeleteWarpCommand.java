/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.argumentparsers.WarpArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

/**
 * Deletes a warp.
 *
 * Command Usage: /warp delete [warp] Permission: quickstart.warp.delete.base
 */
@Permissions(root = "warp")
@RunAsync
@RegisterCommand(value = {"delete", "del"}, subcommandOf = WarpCommand.class)
public class DeleteWarpCommand extends CommandBase<CommandSource> {

    @Inject private WarpConfigAdapter adapter;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new WarpArgument(Text.of(WarpCommand.warpNameArg), adapter, false, false))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WarpArgument.WarpData warp = args.<WarpArgument.WarpData>getOne(WarpCommand.warpNameArg).get();
        NucleusWarpService qs = Sponge.getServiceManager().provideUnchecked(NucleusWarpService.class);

        if (qs.removeWarp(warp.warp)) {
            // Worked. Tell them.
            src.sendMessage(Util.getTextMessageWithFormat("command.warps.del", warp.warp));
            return CommandResult.success();
        }

        // Didn't work. Tell them.
        src.sendMessage(Util.getTextMessageWithFormat("command.warps.delerror"));
        return CommandResult.empty();
    }
}

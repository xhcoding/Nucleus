/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.warp;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.argumentparsers.WarpParser;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.MessageFormat;

/**
 * Deletes a warp.
 *
 * Command Usage: /warp delete [warp] Permission: quickstart.warp.delete.base
 */
@Permissions(root = "warp")
@RunAsync
@RegisterCommand(value = {"delete", "del"}, subcommandOf = WarpCommand.class)
public class DeleteWarpCommand extends CommandBase<CommandSource> {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .arguments(GenericArguments.onlyOne(new WarpParser(Text.of(WarpCommand.warpNameArg), plugin, false, false)))
                .description(Text.of("Deletes a warp.")).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WarpParser.WarpData warp = args.<WarpParser.WarpData>getOne(WarpCommand.warpNameArg).get();
        NucleusWarpService qs = Sponge.getServiceManager().provideUnchecked(NucleusWarpService.class);

        if (qs.removeWarp(warp.warp)) {
            // Worked. Tell them.
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.warps.del"), warp.warp)));
            return CommandResult.success();
        }

        // Didn't work. Tell them.
        src.sendMessage(Util.getTextMessageWithFormat("command.warps.delerror"));
        return CommandResult.empty();
    }
}

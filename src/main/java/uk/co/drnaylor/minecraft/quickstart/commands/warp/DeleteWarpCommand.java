package uk.co.drnaylor.minecraft.quickstart.commands.warp;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarpService;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.WarpParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;

import java.text.MessageFormat;

/**
 * Deletes a warp.
 *
 * Command Usage: /warp delete [warp]
 * Permission: quickstart.warp.delete.base
 */
@Permissions(root = "warp")
@RunAsync
public class DeleteWarpCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .arguments(
                        GenericArguments.onlyOne(new WarpParser(Text.of(WarpsCommand.warpNameArg), plugin, false))
                )
                .description(Text.of("Deletes a warp."))
                .build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "delete", "del" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WarpParser.WarpData warp = args.<WarpParser.WarpData>getOne(WarpsCommand.warpNameArg).get();
        QuickStartWarpService qs = Sponge.getServiceManager().provideUnchecked(QuickStartWarpService.class);

        if (qs.removeWarp(warp.warp)) {
            // Worked. Tell them.
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.warps.del"), warp)));
            return CommandResult.success();
        }

        // Didn't work. Tell them.
        src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.warps.delerror")));
        return CommandResult.empty();
    }
}

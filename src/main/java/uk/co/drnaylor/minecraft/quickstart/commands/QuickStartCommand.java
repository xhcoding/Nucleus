package uk.co.drnaylor.minecraft.quickstart.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.RunAsync;

@RunAsync
@Permissions(QuickStart.PERMISSIONS_PREFIX + "quickstart.base")
public class QuickStartCommand extends CommandBase {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "quickstart" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of(QuickStart.MESSAGE_PREFIX, TextColors.GREEN, "Quick Start version " + QuickStart.VERSION));
        return CommandResult.success();
    }
}

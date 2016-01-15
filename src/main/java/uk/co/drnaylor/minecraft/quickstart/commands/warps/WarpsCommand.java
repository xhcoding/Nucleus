package uk.co.drnaylor.minecraft.quickstart.commands.warps;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.WarpParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

@Permissions(QuickStart.PERMISSIONS_PREFIX + "warp.base")
public class WarpsCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(new WarpParser(Text.of("Warp Name"))).build();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException {
        return null;
    }
}

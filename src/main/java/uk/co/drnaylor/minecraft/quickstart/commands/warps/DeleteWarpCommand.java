package uk.co.drnaylor.minecraft.quickstart.commands.warps;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;

public class DeleteWarpCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] { "del" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException {
        return null;
    }
}

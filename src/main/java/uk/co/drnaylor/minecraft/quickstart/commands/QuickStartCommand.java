package uk.co.drnaylor.minecraft.quickstart.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;

public class QuickStartCommand extends CommandBase {
    public QuickStartCommand(QuickStart plugin) {
        super(plugin);
    }

    @Override
    public CommandSpec getSpec() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] { "quickstart" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException {
        return null;
    }

}

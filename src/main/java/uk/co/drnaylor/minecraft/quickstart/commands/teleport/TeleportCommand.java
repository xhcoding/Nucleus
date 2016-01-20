package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
public class TeleportCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return null;
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

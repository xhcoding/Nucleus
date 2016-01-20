package uk.co.drnaylor.minecraft.quickstart.commands.mute;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;

@Permissions
@RunAsync
@Modules(PluginModule.MUTES)
public class MuteCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] { "mute" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException {
        return null;
    }
}

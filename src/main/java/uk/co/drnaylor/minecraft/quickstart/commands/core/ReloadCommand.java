package uk.co.drnaylor.minecraft.quickstart.commands.core;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

@Permissions(root = "quickstart")
@NoCooldown
@NoCost
@NoWarmup
@RunAsync
public class ReloadCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "reload" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        plugin.reload();
        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.reload.one")));
        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.reload.two")));
        return CommandResult.success();
    }
}

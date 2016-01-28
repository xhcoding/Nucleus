package uk.co.drnaylor.minecraft.quickstart.commands.afk;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

@Permissions
@Modules(PluginModule.AFK)
@NoCooldown
@NoWarmup
@NoCost
@RunAsync
public class AFKCommand extends CommandBase<Player> {
    public static String[] getAfkAliases() { return new String[] { "afk" }; }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return AFKCommand.getAfkAliases();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        return null;
    }
}

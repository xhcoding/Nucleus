package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.NoWarmup;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
public class TeleportCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] { "tp", "teleport" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        return null;
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = super.getDefaults();
        ccn.setComment(Util.messageBundle.getString("config.command.teleport.warmup"));
        return ccn;
    }
}

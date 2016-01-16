package uk.co.drnaylor.minecraft.quickstart.commands.warps;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.WarpParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

@Permissions
public class WarpsCommand extends CommandBase {
    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(GenericArguments.onlyOne(new WarpParser(Text.of(Util.messageBundle.getString("args.name.warpname")), plugin, true))).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "warp" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException {
        WarpParser.WarpData wd = args.<WarpParser.WarpData>getOne(Util.messageBundle.getString("args.name.warpname")).get();
        return null;
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode cn = super.getDefaults();
        cn.getNode("separate-permissions").setComment(Util.messageBundle.getString("config.warps.separate")).setValue(false);
        return cn;
    }
}

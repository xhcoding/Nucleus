package uk.co.drnaylor.minecraft.quickstart.commands.warp;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.WarpParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

import java.text.MessageFormat;

/**
 * Allows a user to warp to the specified warp.
 *
 * Command Usage: /warp [warp]
 * Permission: quickstart.warp.base
 *
 * If <code>warp.separate-permissions</code> = <code>true</code> in the commands config, also requires
 * <code>quickstart.warps.[warpname]</code> permission, or the QuickStart admin permission.
 */
@Permissions
public class WarpsCommand extends CommandBase<Player> {
    static final String seperatePermissionsConfigEntryName = "separate-permissions";
    static final String warpNameArg = Util.messageBundle.getString("args.name.warpname");

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .children(this.createChildCommands(
                        DeleteWarpCommand.class, ListWarpCommand.class, SetWarpCommand.class
                )).arguments(
                        GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.flags().flag("f", "-force").setAnchorFlags(false).buildWith(GenericArguments.none()))),
                        GenericArguments.onlyOne(new WarpParser(Text.of(warpNameArg), plugin, true))
                ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "warp" };
    }

    @Override
    public CommandResult executeCommand(Player pl, CommandContext args) throws Exception {
        // Permission checks are done by the parser.
        WarpParser.WarpData wd = args.<WarpParser.WarpData>getOne(warpNameArg).get();

        // We have a warp data, warp them.
        pl.sendMessage(Text.of(TextColors.YELLOW, MessageFormat.format(Util.messageBundle.getString("command.warps.start"), wd.warp)));

        // Warp them.
        if (args.getOne("f").isPresent()) { // Force the position.
            pl.setLocationAndRotation(wd.loc.getLocation(), wd.loc.getRotation());
        } else if(!pl.setLocationAndRotationSafely(wd.loc.getLocation(), wd.loc.getRotation())) { // No force, try teleport, if failed, tell them.
            pl.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.warps.nosafe")));

            // Don't add the cooldown if enabled.
            return CommandResult.empty();
        }

        return CommandResult.success();
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode cn = super.getDefaults();
        cn.getNode(seperatePermissionsConfigEntryName).setComment(Util.messageBundle.getString("config.warps.separate")).setValue(false);
        return cn;
    }
}

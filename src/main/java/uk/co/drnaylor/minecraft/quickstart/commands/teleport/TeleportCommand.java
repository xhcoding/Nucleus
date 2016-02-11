package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.NoCostArgument;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.NoWarmupArgument;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.RequireMoreArguments;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.RequireOneOfPermission;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

import java.util.Optional;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
public class TeleportCommand extends CommandBase {

    private String[] aliases = null;
    private String playerFromKey = "playerFrom";
    private String playerKey = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                // TODO: Test
                new RequireMoreArguments(GenericArguments.onlyOne(new NoWarmupArgument(new NoCostArgument(new RequireOneOfPermission(GenericArguments.player(Text.of(playerFromKey)), permissions.getPermissionWithSuffix("others"))))), 2),
                GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey)))
        ).build();
    }

    @Override
    public String[] getAliases() {
        if (aliases == null) {
            // Some people want /tp to be held by minecraft. This will allow us to do so.
            if (plugin.getConfig(ConfigMap.COMMANDS_CONFIG).get().getCommandNode("teleport").getNode("use-tp-command").getBoolean(true)) {
                aliases = new String[] { "teleport", "tp" };
            } else {
                aliases = new String[] { "teleport" };
            }
        }

        return aliases;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean noCost = args.<Boolean>getOne(NoCostArgument.NO_COST_ARGUMENT).orElse(false);
        Optional<Player> ofrom = args.<Player>getOne(playerFromKey);
        Player from;
        if (ofrom.isPresent()) {
            from = ofrom.get();
            if (from.equals(src)) {
                src.sendMessage(Text.of(Util.messageBundle.getString("command.teleport.player.noself")));
                return CommandResult.empty();
            }
        } else if (src instanceof Player) {
            from = (Player)src;
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.playeronly")));
            return CommandResult.empty();
        }

        Player pl = args.<Player>getOne(playerKey).get();
        from.setLocationAndRotation(pl.getLocation(), pl.getRotation());
        from.sendMessage(Text.of(Util.getMessageWithFormat("teleport.success", pl.getName())));
        pl.sendMessage(Text.of(Util.getMessageWithFormat("teleport.from.success", from.getName())));

        return CommandResult.success();
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = super.getDefaults();
        ccn.getNode("use-tp-command").setComment(Util.messageBundle.getString("config.command.teleport.tp")).setValue(true);
        return ccn;
    }
}

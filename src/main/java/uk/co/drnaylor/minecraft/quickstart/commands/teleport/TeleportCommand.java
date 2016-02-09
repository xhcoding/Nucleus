package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.NoWarmup;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
public class TeleportCommand extends CommandBase<Player> {

    private String[] aliases = null;
    private String playerKey = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey)))
        ).build();
    }

    @Override
    public String[] getAliases() {
        if (aliases == null) {
            if (plugin.getConfig(ConfigMap.COMMANDS_CONFIG).get().getCommandNode("teleport").getNode("use-tp-command").getBoolean(true)) {
                aliases = new String[] { "teleport", "tp" };
            } else {
                aliases = new String[]{ "teleport" };
            }
        }

        return aliases;
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(playerKey).get();
        double cost = getCost(src);

        if (cost > 0.) {
            plugin.getTpHandler().startTeleport(src, pl, cost, src, true, true);
        } else {
            plugin.getTpHandler().startTeleport(src, pl, true, true);
        }

        return CommandResult.success();
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = super.getDefaults();
        ccn.setComment(Util.messageBundle.getString("config.command.teleport.warmup"));
        ccn.getNode("use-tp-command").setComment(Util.messageBundle.getString("config.command.teleport.tp")).setValue(true);
        return ccn;
    }
}

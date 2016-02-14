package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class TeleportPositionCommand extends CommandBase {
    private final String key = "player";
    private final String location = "location";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.flags().flag("f").buildWith(GenericArguments.none()),
                GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(key))),
                GenericArguments.onlyOne(GenericArguments.location(Text.of(location)))
        ).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "tppos" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(key).get();
        Location<World> loc = args.<Location<World>>getOne(location).get();

        if (args.<Boolean>getOne("f").orElse(false)) {
            pl.sendMessage(Text.of(TextColors.GREEN, Util.messageBundle.getString("command.tppos.success")));
            if (src.equals(pl)) {
                src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tppos.success.other", pl.getName())));
            }

            return CommandResult.success();
        }

        if (pl.setLocationSafely(loc)) {
            pl.sendMessage(Text.of(TextColors.GREEN, Util.messageBundle.getString("command.tppos.success")));
            if (src.equals(pl)) {
                src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tppos.success.other", pl.getName())));
            }

            return CommandResult.success();
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.tppos.nosafe")));
            return CommandResult.empty();
        }
    }
}

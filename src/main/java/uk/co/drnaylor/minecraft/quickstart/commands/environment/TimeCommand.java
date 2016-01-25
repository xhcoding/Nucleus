package uk.co.drnaylor.minecraft.quickstart.commands.environment;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

import java.text.MessageFormat;

@Permissions
@Modules(PluginModule.ENVIRONMENT)
public class TimeCommand extends CommandBase {
    private final String world = "world";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).children(this.createChildCommands(SetTimeCommand.class)).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "time" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties pr = args.<WorldProperties>getOne(world).orElse(null);
        if (pr == null) {
            // Actually, we just care about where we are.
            if (src instanceof Player) {
                pr = ((Player) src).getWorld().getProperties();
            } else if (src instanceof CommandBlockSource) {
                pr = ((CommandBlockSource) src).getWorld().getProperties();
            } else {
                src.sendMessage(Text.of(TextColors.YELLOW, Util.messageBundle.getString("command.settime.default")));
                pr = Sponge.getServer().getDefaultWorld().get();
            }
        }

        src.sendMessage(Text.of(TextColors.YELLOW, MessageFormat.format(Util.messageBundle.getString("command.time"), pr.getWorldName(), Util.getTimeFromTicks(pr.getWorldTime()))));
        return CommandResult.success();
    }
}

package uk.co.drnaylor.minecraft.quickstart.commands.environment;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.storage.WorldProperties;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Permissions(includeUser = true)
@Modules(PluginModule.ENVIRONMENT)
@RootCommand
public class TimeCommand extends CommandBase {
    private final String world = "world";

    @Override
    @SuppressWarnings("unchecked")
    public CommandSpec createSpec() {
        Map<List<String>, CommandCallable> ms = this.createChildCommands(SetTimeCommand.class);
        return CommandSpec.builder().executor(this)
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(world)))))
                .children(ms).build();
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
                src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.settime.default")));
                pr = Sponge.getServer().getDefaultWorld().get();
            }
        }

        src.sendMessage(Text.of(TextColors.YELLOW, MessageFormat.format(Util.getMessageWithFormat("command.time"), pr.getWorldName(), Util.getTimeFromTicks(pr.getWorldTime()))));
        return CommandResult.success();
    }
}

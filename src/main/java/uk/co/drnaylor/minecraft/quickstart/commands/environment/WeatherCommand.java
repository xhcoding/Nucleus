package uk.co.drnaylor.minecraft.quickstart.commands.environment;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.weather.Weather;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.TimespanParser;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.WeatherParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;

import java.text.MessageFormat;
import java.util.Optional;

@Permissions
@Modules(PluginModule.ENVIRONMENT)
@RootCommand
public class WeatherCommand extends CommandBase {
    private final String world = "world";
    private final String weather = "weather";
    private final String duration = "duration";
    private final String timespan = "timespan";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(world)))),
                GenericArguments.onlyOne(new WeatherParser(Text.of(weather))), // More flexible with the arguments we can use.
                GenericArguments.firstParsing(
                    GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.integer(Text.of(duration)))),
                    GenericArguments.onlyOne(GenericArguments.optional(new TimespanParser(Text.of(timespan))))
                )
        ).description(Text.of("Sets the weather")).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "weather" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // We can predict the weather on multiple worlds now!
        WorldProperties wp = args.<WorldProperties>getOne(world).orElse(null);
        World w;
        if (wp != null) {
            w = Sponge.getServer().getWorld(wp.getUniqueId()).get();
        } else {
            // Actually, we just care about where we are.
            if (src instanceof Player) {
                w = ((Player) src).getWorld();
            } else if (src instanceof CommandBlockSource) {
                w = ((CommandBlockSource) src).getWorld();
            } else {
                // As supreme overlord of the worlds... you have to specify one.
                throw new CommandException(Text.of("The world must be specified."));
            }
        }

        // Houston, we have a world! Now, what was the forecast?
        Weather we = args.<Weather>getOne(weather).get();

        // Have we gotten an accurate forecast? Do we know how long this weather spell will go on for?
        Optional<Long> oi = args.<Long>getOne(timespan);
        if (!oi.isPresent()) {
            Optional<Integer> i = args.<Integer>getOne(duration);
            oi =  i.isPresent() ? Optional.of((long)i.get()) : Optional.empty();
        }

        if (oi.isPresent()) {
            // YES! I should get a job at the weather service and show them how it's done!
            w.forecast(we, oi.get());
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.weather.time"), we.getName(), w.getName(), Util.getTimeStringFromSeconds(oi.get()))));
        } else {
            // No, probably because I've already gotten a job at the weather service...
            w.forecast(we);
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString("command.weather"), we.getName(), w.getName())));
        }

        // The weather control device has been activated!
        return CommandResult.success();
    }
}

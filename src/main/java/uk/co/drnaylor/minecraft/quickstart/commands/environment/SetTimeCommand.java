/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.environment;

import org.spongepowered.api.Sponge;
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
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.WorldTimeParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.ChildOf;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

import java.text.MessageFormat;

@Permissions(root = "time")
@ChildOf(parentCommandClass = TimeCommand.class, parentCommand = "time")
public class SetTimeCommand extends CommandBase<CommandSource> {
    private final String time = "time";
    private final String world = "world";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).description(Text.of("Sets the time")).arguments(
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(world)))),
                GenericArguments.onlyOne(new WorldTimeParser(Text.of(time)))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "set" };
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

        int tick = args.<Integer>getOne(time).get();
        pr.setWorldTime(tick);
        src.sendMessage(Text.of(TextColors.YELLOW, MessageFormat.format(Util.getMessageWithFormat("command.settime.done"), Util.getTimeFromTicks(tick))));
        return CommandResult.success();
    }
}

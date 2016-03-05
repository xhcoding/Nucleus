/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.environment;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.ENVIRONMENT)
@RegisterCommand("time")
public class TimeCommand extends CommandBase<CommandSource> {

    private final String world = "world";

    @Override
    public CommandSpec createSpec() {
        Map<List<String>, CommandCallable> ms = this.createChildCommands(SetTimeCommand.class);
        return CommandSpec.builder().executor(this)
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(world))))).children(ms).build();
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
                src.sendMessage(Util.getTextMessageWithFormat("command.settime.default"));
                pr = Sponge.getServer().getDefaultWorld().get();
            }
        }

        src.sendMessage(Text.of(TextColors.YELLOW,
                MessageFormat.format(Util.getMessageWithFormat("command.time"), pr.getWorldName(), Util.getTimeFromTicks(pr.getWorldTime()))));
        return CommandResult.success();
    }
}

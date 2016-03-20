/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.List;
import java.util.Map;

@Permissions(suggestedLevel = SuggestedLevel.USER)
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
        WorldProperties pr = getWorldPropertiesOrDefault(src, world, args);

        src.sendMessage(Util.getTextMessageWithFormat("command.time", pr.getWorldName(), String.valueOf(Util.getTimeFromTicks(pr.getWorldTime()))));
        return CommandResult.success();
    }
}

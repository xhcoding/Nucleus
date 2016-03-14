/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.environment;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.data.NucleusWorld;
import io.github.nucleuspowered.nucleus.config.loaders.WorldConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

/**
 * Locks (or unlocks) the weather.
 *
 * Permission: nucleus.lockweather.base
 */
@Permissions
@RunAsync
@Modules(PluginModule.ENVIRONMENT)
@RegisterCommand({ "lockweather", "killweather" })
@NoWarmup
@NoCooldown
@NoCost
public class LockWeatherCommand extends CommandBase<CommandSource> {

    @Inject private WorldConfigLoader loader;

    private final String worldKey = "world";
    private final String toggleKey = "toggle";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.world(Text.of(worldKey)))),
                GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.bool(Text.of(toggleKey))))
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<WorldProperties> world = getWorldProperties(src, worldKey, args);
        if (!world.isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.specifyworld"));
            return CommandResult.empty();
        }

        WorldProperties wp = world.get();
        NucleusWorld ws = loader.getWorld(wp.getUniqueId());
        boolean toggle = args.<Boolean>getOne(toggleKey).orElse(!ws.isLockWeather());

        ws.setLockWeather(toggle);
        if (toggle) {
            src.sendMessage(Util.getTextMessageWithFormat("command.lockweather.locked", wp.getWorldName()));
        } else {
            src.sendMessage(Util.getTextMessageWithFormat("command.lockweather.unlocked", wp.getWorldName()));
        }

        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.SkipOnError;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;

import java.util.Optional;
import java.util.Set;

public class CommandBuilder {

    private final Nucleus plugin;
    private final Injector injector;
    private final Set<Class<? extends AbstractCommand<?>>> commandSet;
    private final SimpleCommentedConfigurationNode sn;

    public CommandBuilder(Nucleus plugin, Injector injector, Set<Class<? extends AbstractCommand<?>>> commandSet) {
        this.plugin = plugin;
        this.injector = injector;
        this.commandSet = commandSet;
        this.sn = SimpleCommentedConfigurationNode.root();
    }

    public <T extends AbstractCommand<?>> Optional<T> buildCommand(Class<T> commandClass) {
        return buildCommand(commandClass, true);
    }

    <T extends AbstractCommand<?>> Optional<T> buildCommand(Class<T> commandClass, boolean rootCmd) {
        Optional<T> optionalCommand = getInstance(commandClass);
        if (!optionalCommand.isPresent()) {
            return Optional.empty();
        }

        T c = optionalCommand.get();
        try {
            c.setModuleCommands(commandSet);
            c.setCommandBuilder(this);
            c.postInit();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        // No spec, no return. We also don't want to run it twice...
        CommandSpec spec = c.getSpec();
        if (spec == null) {
            return Optional.empty();
        }

        if (c.mergeDefaults()) {
            sn.getNode(c.getCommandConfigAlias()).setValue(c.getDefaults());
        }

        if (plugin.getCommandsConfig().getCommandNode(c.getCommandConfigAlias()).getNode("enabled").getBoolean(true)) {
            // Register the commands.
            if (rootCmd) {
                Sponge.getCommandManager().register(plugin, spec, c.getAliases());

                // Register as another full blown command.
                for (String s : c.getForcedAliases()) {
                    Sponge.getCommandManager().register(plugin, spec, s);
                }
            }

            return Optional.of(c);
        }

        return Optional.empty();
    }

    private <T> Optional<T> getInstance(Class<T> clazz) {
        try {
            return Optional.of(injector.getInstance(clazz));

            // I can't believe I have to do this...
        } catch (RuntimeException | NoClassDefFoundError e) {
            if (clazz.isAnnotationPresent(SkipOnError.class)) {
                plugin.getLogger().warn(Util.getMessageWithFormat("startup.injectablenotloaded", clazz.getName()));
                return Optional.empty();
            }

            throw e;
        }
    }

    public CommentedConfigurationNode getNodeToMerge() {
        return sn;
    }
}

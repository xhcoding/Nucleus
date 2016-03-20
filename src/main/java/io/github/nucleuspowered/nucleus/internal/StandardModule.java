/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import uk.co.drnaylor.quickstart.Module;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class StandardModule implements Module {

    @Inject protected Nucleus nucleus;
    @Inject protected InternalServiceManager serviceManager;

    @Inject
    private CommandsConfig commandsConfig;

    @Override
    public void onEnable() {
        try {
            performPreTasks();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot enable module!", e);
        }

        // Construct commands
        try {
            loadCommands();
            loadEvents();
            loadRunnables();
        } catch (IOException e) {
            throw new RuntimeException("Error", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCommands() throws IOException {
        Set<Class<? extends CommandBase>> commandBases;
        commandBases = Util.getClasses(CommandBase.class, this.getClass().getPackage().getName()).stream().filter(x -> {
                RegisterCommand rc = x.getAnnotation(RegisterCommand.class);
                return (rc != null && rc.subcommandOf().equals(CommandBase.class));
            }).collect(Collectors.toSet());

        Injector injector = nucleus.getInjector();
        CommentedConfigurationNode sn = SimpleCommentedConfigurationNode.root();
        commandBases.stream().map(x -> {
            try {
                return injector.getInstance(x);
            } catch (Exception e) {
                return null;
            }
        }).filter(x -> x != null).forEach(c -> {
            try {
                c.postInit();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // No spec, no return. We also don't want to run it twice...
            CommandSpec spec = c.getSpec();
            if (spec == null) {
                return;
            }

            // Merge in config defaults.
            if (c.mergeDefaults()) {
                sn.getNode(c.getCommandConfigAlias()).setValue(c.getDefaults());
            }

            if (commandsConfig.getCommandNode(c.getCommandConfigAlias()).getNode("enabled").getBoolean(true)) {
                // Register the commands.
                Sponge.getCommandManager().register(nucleus, spec, c.getAliases());
            }
        });

        try {
            commandsConfig.mergeDefaults(sn);
            commandsConfig.save();
        } catch (IOException | ObjectMappingException e) {
            nucleus.getLogger().error("Could not save defaults.");
            e.printStackTrace();
        }
    }

    private void loadEvents() throws IOException {
        Set<Class<? extends ListenerBase>> commandsToLoad = Util.getClasses(ListenerBase.class, this.getClass().getPackage().getName());
        Injector injector = nucleus.getInjector();
        commandsToLoad.stream().map(x -> {
            try {
                ListenerBase lb = x.newInstance();
                injector.injectMembers(lb);
                return lb;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(lb -> lb != null).forEach(c -> {
            // Register suggested permissions
            c.getPermissions().forEach((k, v) -> nucleus.getPermissionRegistry().registerOtherPermission(k, v));
            Sponge.getEventManager().registerListeners(nucleus, c);
        });
    }

    private void loadRunnables() throws IOException {
        Set<Class<? extends TaskBase>> commandsToLoad = Util.getClasses(TaskBase.class, this.getClass().getPackage().getName());
        Injector injector = nucleus.getInjector();
        commandsToLoad.stream().map(x -> {
            try {
                TaskBase lb = x.newInstance();
                injector.injectMembers(lb);
                return lb;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(lb -> lb != null).forEach(c -> {
            c.getPermissions().forEach((k, v) -> nucleus.getPermissionRegistry().registerOtherPermission(k, v));
            Task.Builder tb = Sponge.getScheduler().createTaskBuilder().execute(c).interval(c.secondsPerRun(), TimeUnit.SECONDS);
            if (c.isAsync()) {
                tb.async();
            }

            tb.submit(nucleus);
        });
    }

    protected void performPreTasks() throws Exception { }
}

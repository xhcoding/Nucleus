/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.internal.annotations.ModuleCommandSet;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
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
        Set<Class<? extends AbstractCommand>> cmds = nucleus.getModuleContainer().getLoadedClasses().stream().filter(AbstractCommand.class::isAssignableFrom)
                .filter(x -> x.getPackage().getName().startsWith(this.getClass().getPackage().getName()))
                .filter(x -> x.isAnnotationPresent(RegisterCommand.class))
                .map(x -> (Class<? extends AbstractCommand>)x)
                .collect(Collectors.toSet());

        // We all love the special injector. We just want to provide the module with more commands, in case it needs a child.
        Injector injector = nucleus.getInjector().createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<Set<Class<? extends AbstractCommand>>>(){}).annotatedWith(ModuleCommandSet.class).toInstance(cmds);
            }
        });

        Set<Class<? extends AbstractCommand>> commandBases =  cmds.stream().filter(x -> {
            RegisterCommand rc = x.getAnnotation(RegisterCommand.class);
            return (rc != null && rc.subcommandOf().equals(AbstractCommand.class));
        }).collect(Collectors.toSet());

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

    @SuppressWarnings("unchecked")
    private void loadEvents() throws IOException {
        Set<Class<? extends ListenerBase>> commandsToLoad = nucleus.getModuleContainer().getLoadedClasses().stream()
                .filter(ListenerBase.class::isAssignableFrom)
                .filter(x -> x.getPackage().getName().startsWith(this.getClass().getPackage().getName()))
                .map(x -> (Class<? extends ListenerBase>)x)
                .collect(Collectors.toSet());

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
        Set<Class<? extends TaskBase>> commandsToLoad = nucleus.getModuleContainer().getLoadedClasses().stream()
                .filter(TaskBase.class::isAssignableFrom)
                .filter(x -> x.getPackage().getName().startsWith(this.getClass().getPackage().getName()))
                .map(x -> (Class<? extends TaskBase>)x)
                .collect(Collectors.toSet());

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

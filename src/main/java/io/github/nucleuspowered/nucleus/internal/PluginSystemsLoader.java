/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.reflect.ClassPath;
import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PluginSystemsLoader {
    private final Nucleus nucleus;

    public PluginSystemsLoader(Nucleus nucleus) {
        this.nucleus = nucleus;
    }

    private final NucleusModuleService service = Sponge.getServiceManager().provideUnchecked(NucleusModuleService.class);

    private final Predicate<Class<?>> moduleCheck = o -> {
        Modules annotation = o.getAnnotation(Modules.class);
        // No annotation, include it.
        return annotation == null || service.getModulesToLoad().stream().anyMatch(a -> Arrays.asList(annotation.value()).contains(a));
    };

    <T> Set<Class<? extends T>> filterOutModules(Set<Class<? extends T>> objectsToFilter) {
        return objectsToFilter.stream().filter(moduleCheck).collect(Collectors.toSet());
    }

    public void load() throws IOException {
        loadCommands();
        loadEvents();
        loadRunnables();

        // Register permissions
        Optional<PermissionService> ops = Sponge.getServiceManager().provide(PermissionService.class);
        if (ops.isPresent()) {
            Optional<PermissionDescription.Builder> opdb = ops.get().newDescriptionBuilder(nucleus);
            if (opdb.isPresent()) {
                Map<String, PermissionInformation> m = nucleus.getPermissionRegistry().getPermissions();
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.ADMIN).forEach(k -> ops.get().newDescriptionBuilder(nucleus).get().assign(PermissionDescription.ROLE_ADMIN, true).description(k.getValue().description).id(k.getKey()).register());
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.MOD).forEach(k -> ops.get().newDescriptionBuilder(nucleus).get().assign(PermissionDescription.ROLE_STAFF, true).description(k.getValue().description).id(k.getKey()).register());
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.USER).forEach(k -> ops.get().newDescriptionBuilder(nucleus).get().assign(PermissionDescription.ROLE_USER, true).description(k.getValue().description).id(k.getKey()).register());
            }
        }
    }

    static <T> Set<Class<? extends T>> getClasses(Class<T> base, String pack) throws IOException {
        Set<ClassPath.ClassInfo> ci = ClassPath.from(PluginSystemsLoader.class.getClassLoader()).getTopLevelClassesRecursive(pack);
        return ci.stream().map(ClassPath.ClassInfo::load).map(x -> x.asSubclass(base)).collect(Collectors.toSet());
    }

    static Set<Class<? extends CommandBase>> getCommandClasses(Class<? extends CommandBase> base) throws IOException {
        Set<Class<? extends CommandBase>> cc = getClasses(CommandBase.class, "io.github.nucleuspowered.nucleus.commands");
        return cc.stream().filter(x -> {
            RegisterCommand rc = x.getAnnotation(RegisterCommand.class);
            return (rc != null && rc.subcommandOf().equals(base));
        }).collect(Collectors.toSet());
    }

    private void loadCommands() throws IOException {
        // Get commands
        Set<Class<? extends CommandBase>> commandsToLoad = filterOutModules(getCommandClasses(CommandBase.class));
        Injector injector = nucleus.getInjector();

        // Commands config!
        CommandsConfig cc = nucleus.getConfig(ConfigMap.COMMANDS_CONFIG).get();
        CommentedConfigurationNode sn = SimpleCommentedConfigurationNode.root();
        commandsToLoad.stream().map(x -> {
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

            // Register the commands.
            Sponge.getCommandManager().register(nucleus, spec, c.getAliases());
        });

        try {
            cc.mergeDefaults(sn);
            cc.save();
        } catch (IOException | ObjectMappingException e) {
            nucleus.getLogger().error("Could not save defaults.");
            e.printStackTrace();
        }
    }

    private void loadEvents() throws IOException {
        Set<Class<? extends ListenerBase>> commandsToLoad = filterOutModules(getClasses(ListenerBase.class, "io.github.nucleuspowered.nucleus.listeners"));
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
        Set<Class<? extends TaskBase>> commandsToLoad = filterOutModules(getClasses(TaskBase.class, "io.github.nucleuspowered.nucleus.runnables"));
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
}

/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal;

import com.google.common.reflect.ClassPath;
import com.google.inject.Injector;
import io.github.essencepowered.essence.QuickStart;
import io.github.essencepowered.essence.api.service.QuickStartModuleService;
import io.github.essencepowered.essence.config.CommandsConfig;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
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
    private final QuickStart quickStart;

    public PluginSystemsLoader(QuickStart quickStart) {
        this.quickStart = quickStart;
    }

    private final QuickStartModuleService service = Sponge.getServiceManager().provideUnchecked(QuickStartModuleService.class);

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
            Optional<PermissionDescription.Builder> opdb = ops.get().newDescriptionBuilder(quickStart);
            if (opdb.isPresent()) {
                Map<String, PermissionInformation> m = quickStart.getPermissionRegistry().getPermissions();
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.ADMIN).forEach(k -> ops.get().newDescriptionBuilder(quickStart).get().assign(PermissionDescription.ROLE_ADMIN, true).description(k.getValue().description).id(k.getKey()).register());
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.MOD).forEach(k -> ops.get().newDescriptionBuilder(quickStart).get().assign(PermissionDescription.ROLE_STAFF, true).description(k.getValue().description).id(k.getKey()).register());
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.USER).forEach(k -> ops.get().newDescriptionBuilder(quickStart).get().assign(PermissionDescription.ROLE_USER, true).description(k.getValue().description).id(k.getKey()).register());
            }
        }
    }

    static <T> Set<Class<? extends T>> getClasses(Class<T> base, String pack) throws IOException {
        Set<ClassPath.ClassInfo> ci = ClassPath.from(PluginSystemsLoader.class.getClassLoader()).getTopLevelClassesRecursive(pack);
        return ci.stream().map(ClassPath.ClassInfo::load).map(x -> x.asSubclass(base)).collect(Collectors.toSet());
    }

    static Set<Class<? extends CommandBase>> getCommandClasses(Class<? extends CommandBase> base) throws IOException {
        Set<Class<? extends CommandBase>> cc = getClasses(CommandBase.class, "uk.co.drnaylor.minecraft.quickstart.commands");
        return cc.stream().filter(x -> {
            RegisterCommand rc = x.getAnnotation(RegisterCommand.class);
            return (rc != null && rc.subcommandOf().equals(base));
        }).collect(Collectors.toSet());
    }

    private void loadCommands() throws IOException {
        // Get commands
        Set<Class<? extends CommandBase>> commandsToLoad = filterOutModules(getCommandClasses(CommandBase.class));
        Injector injector = quickStart.getInjector();

        // Commands config!
        CommandsConfig cc = quickStart.getConfig(ConfigMap.COMMANDS_CONFIG).get();
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
            Sponge.getCommandManager().register(quickStart, spec, c.getAliases());
        });

        try {
            cc.mergeDefaults(sn);
            cc.save();
        } catch (IOException | ObjectMappingException e) {
            quickStart.getLogger().error("Could not save defaults.");
            e.printStackTrace();
        }
    }

    private void loadEvents() throws IOException {
        Set<Class<? extends ListenerBase>> commandsToLoad = filterOutModules(getClasses(ListenerBase.class, "uk.co.drnaylor.minecraft.quickstart.listeners"));
        Injector injector = quickStart.getInjector();
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
            c.getPermissions().forEach((k, v) -> quickStart.getPermissionRegistry().registerOtherPermission(k, v));
            Sponge.getEventManager().registerListeners(quickStart, c);
        });
    }

    private void loadRunnables() throws IOException {
        Set<Class<? extends TaskBase>> commandsToLoad = filterOutModules(getClasses(TaskBase.class, "uk.co.drnaylor.minecraft.quickstart.runnables"));
        Injector injector = quickStart.getInjector();
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
            c.getPermissions().forEach((k, v) -> quickStart.getPermissionRegistry().registerOtherPermission(k, v));
            Task.Builder tb = Sponge.getScheduler().createTaskBuilder().execute(c).interval(c.secondsPerRun(), TimeUnit.SECONDS);
            if (c.isAsync()) {
                tb.async();
            }

            tb.submit(quickStart);
        });
    }
}

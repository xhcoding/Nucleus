/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.reflect.ClassPath;
import com.google.inject.Injector;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.enums.SuggestedLevel;

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
                Map<String, SuggestedLevel> m = quickStart.getPermissionRegistry().getPermissions();
                m.entrySet().stream().filter(x -> x.getValue() == SuggestedLevel.ADMIN).map(Map.Entry::getKey).forEach(k -> ops.get().newDescriptionBuilder(quickStart).get().assign(PermissionDescription.ROLE_ADMIN, true).id(k).register());
                m.entrySet().stream().filter(x -> x.getValue() == SuggestedLevel.MOD).map(Map.Entry::getKey).forEach(k -> ops.get().newDescriptionBuilder(quickStart).get().assign(PermissionDescription.ROLE_STAFF, true).id(k).register());
                m.entrySet().stream().filter(x -> x.getValue() == SuggestedLevel.USER).map(Map.Entry::getKey).forEach(k -> ops.get().newDescriptionBuilder(quickStart).get().assign(PermissionDescription.ROLE_USER, true).id(k).register());
            }
        }
    }

    private <T> Set<Class<? extends T>> getClasses(Class<T> base, String pack) throws IOException {
        Set<ClassPath.ClassInfo> ci = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive(pack);
        return ci.stream().map(ClassPath.ClassInfo::load).map(x -> x.asSubclass(base)).collect(Collectors.toSet());
    }

    private void loadCommands() throws IOException {
        // Get commands
        Set<Class<? extends CommandBase>> commandsToLoad = filterOutModules(getClasses(CommandBase.class, "uk.co.drnaylor.minecraft.quickstart.commands"));
        Injector injector = quickStart.getInjector();

        // Commands config!

        CommandsConfig cc = quickStart.getConfig(ConfigMap.COMMANDS_CONFIG).get();
        CommentedConfigurationNode sn = SimpleCommentedConfigurationNode.root();
        commandsToLoad.stream().map(x -> {
            if (!x.isAnnotationPresent(RootCommand.class)) {
                // If not a root command, return nothing.
                return null;
            }

            try {
                return injector.getInstance(x);
            } catch (Exception e) {
                return null;
            }
        }).filter(x -> x != null).forEach(c -> {
            try {
                c.postInit();
            } catch (Exception e) {
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
            System.out.println(c.getClass().getName());
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

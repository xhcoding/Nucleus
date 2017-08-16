/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml.module;

import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresPlatform;
import io.github.nucleuspowered.nucleus.internal.annotations.SkipOnError;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Scan;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBuilder;
import io.github.nucleuspowered.nucleus.internal.docgen.DocGenCache;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.playerinfo.handlers.BasicSeenInformationProvider;
import io.github.nucleuspowered.nucleus.modules.playerinfo.handlers.SeenHandler;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.inject.Inject;

public abstract class StandardModule implements Module {

    private final String moduleId;
    private final String moduleName;
    private String packageName;
    @Inject protected NucleusPlugin plugin;
    @Inject protected InternalServiceManager serviceManager;

    @Inject
    private CommandsConfig commandsConfig;

    public StandardModule() {
        ModuleData md = this.getClass().getAnnotation(ModuleData.class);
        this.moduleId = md.id();
        this.moduleName = md.name();
    }

    /**
     * Non-configurable module, no configuration to register.
     *
     * @return {@link Optional#empty()}
     */
    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.empty();
    }

    @Override
    public final void preEnable() {
        try {
            performPreTasks();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot enable module!", e);
        }
    }

    @Override
    public void onEnable() {
        packageName = this.getClass().getPackage().getName() + ".";

        // Construct commands
        loadCommands();
        loadEvents();
        loadRunnables();
    }

    @SuppressWarnings("unchecked")
    private void loadCommands() {

        Set<Class<? extends AbstractCommand<?>>> cmds = new HashSet<>(
            performFilter(getStreamForModule(AbstractCommand.class).map(x -> (Class<? extends AbstractCommand<?>>)x))
                .collect(Collectors.toSet()));

        // Find all commands that are also scannable.
        performFilter(plugin.getModuleContainer().getLoadedClasses().stream()
            .filter(x -> x.getPackage().getName().startsWith(packageName))
            .filter(x -> x.isAnnotationPresent(Scan.class))
            .flatMap(x -> Arrays.stream(x.getDeclaredClasses()))
            .filter(AbstractCommand.class::isAssignableFrom)
            .map(x -> (Class<? extends AbstractCommand<?>>)x))
            .forEach(cmds::add);

        // We all love the special injector. We just want to provide the module with more commands, in case it needs a child.
        Injector injector = plugin.getInjector();

        Set<Class<? extends AbstractCommand>> commandBases =  cmds.stream().filter(x -> {
            RegisterCommand rc = x.getAnnotation(RegisterCommand.class);
            return (rc != null && rc.subcommandOf().equals(AbstractCommand.class));
        }).collect(Collectors.toSet());

        CommandBuilder builder = new CommandBuilder(plugin, injector, cmds, moduleId, moduleName);
        commandBases.forEach(builder::buildCommand);

        try {
            commandsConfig.mergeDefaults(builder.getNodeToMerge());
            commandsConfig.save();
        } catch (Exception e) {
            plugin.getLogger().error("Could not save defaults.");
            e.printStackTrace();
        }
    }

    private Stream<Class<? extends AbstractCommand<?>>> performFilter(Stream<Class<? extends AbstractCommand<?>>> stream) {
        return stream.filter(x -> x.isAnnotationPresent(RegisterCommand.class))
            .map(x -> (Class<? extends AbstractCommand<?>>)x); // Keeping the compiler happy...
    }

    @SuppressWarnings("unchecked")
    private void loadEvents() {
        Set<Class<? extends ListenerBase>> listenersToLoad = getStreamForModule(ListenerBase.class)
            .collect(Collectors.toSet());

        Optional<DocGenCache> docGenCache = plugin.getDocGenCache();
        Injector injector = plugin.getInjector();
        listenersToLoad.stream().map(x -> this.getInstance(injector, x)).filter(Objects::nonNull).forEach(c -> {
            // Register suggested permissions
            c.getPermissions().forEach((k, v) -> plugin.getPermissionRegistry().registerOtherPermission(k, v));
            docGenCache.ifPresent(x -> x.addPermissionDocs(moduleId, c.getPermissions()));

            final ConditionalListener conditionalListener = c.getClass().getAnnotation(ConditionalListener.class);
            if (conditionalListener != null) {
                try {
                    Predicate<Nucleus> cl = conditionalListener.value().newInstance();
                    Reloadable tae = () -> {
                        Sponge.getEventManager().unregisterListeners(c);
                        if (cl.test(plugin)) {
                            if (c instanceof Reloadable) {
                                ((Reloadable) c).onReload();
                            }
                            Sponge.getEventManager().registerListeners(plugin, c);
                        }
                    };

                    // Add reloadable to load in the listener dynamically if required.
                    plugin.registerReloadable(tae);
                    tae.onReload();
                } catch (Exception e) {
                    if (plugin.isDebugMode()) {
                        e.printStackTrace();
                    }
                }
            } else if (c instanceof ListenerBase.Conditional) {
                // Add reloadable to load in the listener dynamically if required.
                Reloadable tae = () -> {
                    Sponge.getEventManager().unregisterListeners(c);
                    if (((ListenerBase.Conditional) c).shouldEnable()) {
                        if (c instanceof Reloadable) {
                            ((Reloadable) c).onReload();
                        }
                        Sponge.getEventManager().registerListeners(plugin, c);
                    }
                };

                plugin.registerReloadable(tae);
                try {
                    tae.onReload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (c instanceof Reloadable) {
                plugin.registerReloadable(((Reloadable) c));
                Sponge.getEventManager().registerListeners(plugin, c);
            } else {
                Sponge.getEventManager().registerListeners(plugin, c);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadRunnables() {
        Set<Class<? extends TaskBase>> commandsToLoad = getStreamForModule(TaskBase.class)
                .collect(Collectors.toSet());

        Optional<DocGenCache> docGenCache = plugin.getDocGenCache();
        Injector injector = plugin.getInjector();
        commandsToLoad.stream().map(x -> this.getInstance(injector, x)).filter(Objects::nonNull).forEach(c -> {
            c.getPermissions().forEach((k, v) -> plugin.getPermissionRegistry().registerOtherPermission(k, v));
            docGenCache.ifPresent(x -> x.addPermissionDocs(moduleId, c.getPermissions()));
            Task.Builder tb = Sponge.getScheduler().createTaskBuilder().interval(c.interval().toMillis(), TimeUnit.MILLISECONDS);
            if (Nucleus.getNucleus().isServer()) {
                tb.execute(c);
            } else {
                tb.execute(t -> {
                    if (Sponge.getGame().isServerAvailable()) {
                        c.accept(t);
                    }
                });
            }

            if (c.isAsync()) {
                tb.async();
            }

            tb.submit(plugin);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Stream<Class<? extends T>> getStreamForModule(Class<T> assignableClass) {
        return Nucleus.getNucleus().getModuleContainer().getLoadedClasses().stream()
                .filter(assignableClass::isAssignableFrom)
                .filter(x -> x.getPackage().getName().startsWith(packageName))
                .filter(x -> !Modifier.isAbstract(x.getModifiers()) && !Modifier.isInterface(x.getModifiers()))
                .filter(this::checkPlatform)
                .map(x -> (Class<? extends T>)x);
    }

    protected void performPreTasks() throws Exception { }

    private <T> T getInstance(Injector injector, Class<T> clazz) {
        try {
            return injector.getInstance(clazz);

        // I can't believe I have to do this...
        } catch (RuntimeException | NoClassDefFoundError e) {
            if (clazz.isAnnotationPresent(SkipOnError.class)) {
                plugin.getLogger().warn(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("startup.injectablenotloaded", clazz.getName()));
                return null;
            }

            throw e;
        }
    }

    private <T extends Class<?>> boolean checkPlatform(T clazz) {
        if (clazz.isAnnotationPresent(RequiresPlatform.class)) {
            String platformId = Sponge.getPlatform().getContainer(Platform.Component.GAME).getId();
            boolean loadable = Arrays.stream(clazz.getAnnotation(RequiresPlatform.class).value()).anyMatch(platformId::equalsIgnoreCase);
            if (!loadable) {
                plugin.getLogger().warn("Not loading /" + clazz.getSimpleName() + ": platform " + platformId + " is not supported.");
                return false;
            }
        }

        return true;
    }

    protected final void createSeenModule(BiFunction<CommandSource, User, Collection<Text>> function) {
        createSeenModule((String)null, function);
    }

    protected final void createSeenModule(@Nullable Class<? extends AbstractCommand> permissionClass, BiFunction<CommandSource, User, Collection<Text>> function) {
        // Register seen information.
        CommandPermissionHandler permissionHandler = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(permissionClass);
        createSeenModule(permissionHandler == null ? null : permissionHandler.getBase(), function);
    }

    protected final void createSeenModule(@Nullable Class<? extends AbstractCommand> permissionClass, String suffix, BiFunction<CommandSource, User, Collection<Text>> function) {
        // Register seen information.
        CommandPermissionHandler permissionHandler = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(permissionClass);
        createSeenModule(permissionHandler == null ? null : permissionHandler.getPermissionWithSuffix(suffix), function);
    }

    private void createSeenModule(@Nullable String permission, BiFunction<CommandSource, User, Collection<Text>> function) {
        plugin.getInternalServiceManager().getService(SeenHandler.class).ifPresent(x -> x.register(plugin, this.getClass().getAnnotation(ModuleData.class).name(),
            new BasicSeenInformationProvider(permission, function)));
    }
}

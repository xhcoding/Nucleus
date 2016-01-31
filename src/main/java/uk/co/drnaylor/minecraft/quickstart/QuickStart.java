package uk.co.drnaylor.minecraft.quickstart;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.ModulesLoadedException;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.UnremovableModuleException;
import uk.co.drnaylor.minecraft.quickstart.api.service.*;
import uk.co.drnaylor.minecraft.quickstart.config.AbstractConfig;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.config.WarpsConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandLoader;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.EventLoader;
import uk.co.drnaylor.minecraft.quickstart.internal.handlers.AFKHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.handlers.MailHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.handlers.MessageHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.guice.QuickStartInjectorModule;
import uk.co.drnaylor.minecraft.quickstart.internal.services.ModuleRegistration;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;
import uk.co.drnaylor.minecraft.quickstart.internal.services.WarmupManager;
import uk.co.drnaylor.minecraft.quickstart.runnables.AFKTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Plugin(id = QuickStart.ID, name = QuickStart.NAME, version = QuickStart.VERSION)
public class QuickStart {
    public final static String ID = "quickstart";
    public final static String NAME = "Quick Start";
    public final static String VERSION = "0.1";
    public final static String PERMISSIONS_PREFIX = "quickstart.";
    public final static String PERMISSIONS_ADMIN = PERMISSIONS_PREFIX + "admin";
    public final static Text MESSAGE_PREFIX = Text.of(TextColors.GREEN, "[" + NAME + "] ");
    public final static Text ERROR_MESSAGE_PREFIX = Text.of(TextColors.RED, "[" + NAME + "] ");

    private ModuleRegistration moduleRegistration;
    private boolean modulesLoaded = false;
    private boolean isErrored = false;
    private final ConfigMap configMap = new ConfigMap();
    private UserConfigLoader configLoader;
    private Injector injector;
    private MessageHandler messageHandler = new MessageHandler();
    private MailHandler mailHandler;

    private AFKHandler afkHandler = new AFKHandler();

    @Inject private Game game;
    @Inject private Logger logger;
    @Inject @DefaultConfig(sharedRoot = false) private Path path;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private Path dataDir;

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitializationEvent) {
        dataDir = game.getSavesDirectory().resolve("quickstart-essentials");
        // Get the mandatory config files.
        try {
            Files.createDirectories(dataDir);
            configMap.putConfig(new MainConfig(path));
            configMap.putConfig(new CommandsConfig(Paths.get(configDir.toString(), "commands.conf")));
            configLoader = new UserConfigLoader(this);
            moduleRegistration = new ModuleRegistration(this);
        } catch (IOException | ObjectMappingException e) {
            isErrored = true;
            e.printStackTrace();
            return;
        }

        // We register the ModuleService NOW so that others can hook into it.
        game.getServiceManager().setProvider(this, QuickStartModuleService.class, moduleRegistration);
        game.getServiceManager().setProvider(this, QuickStartWarmupManagerService.class, new WarmupManager());
        this.injector = Guice.createInjector(new QuickStartInjectorModule(this, configLoader));
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        if (isErrored) {
            return;
        }

        Set<PluginModule> modules = moduleRegistration.getModulesToLoad();

        // Load the following services only if necessary.
        if (modules.contains(PluginModule.WARPS)) {
            try {
                configMap.putConfig(new WarpsConfig(Paths.get(dataDir.toString(), "warp.json")));

                // Put the warp service into the service manager.
                game.getServiceManager().setProvider(this, QuickStartWarpService.class, configMap.getConfig(WarpsConfig.class).get());
            } catch (IOException | ObjectMappingException ex) {
                try {
                    moduleRegistration.removeModule(PluginModule.WARPS);
                } catch (ModulesLoadedException | UnremovableModuleException e) {
                    // Nope.
                }

                logger.warn("Could not load the warp module for the reason below.");
                ex.printStackTrace();
            }
        }

        if (modules.contains(PluginModule.AFK)) {
            Sponge.getScheduler().createTaskBuilder().async().name("QuickStart - AFK").delay(2, TimeUnit.SECONDS)
                    .interval(2, TimeUnit.SECONDS).execute(new AFKTask(this)).submit(this);
        }

        if (modules.contains(PluginModule.MAILS)) {
            mailHandler = new MailHandler(game, this);
            game.getServiceManager().setProvider(this, QuickStartMailService.class, mailHandler);
        }

        modulesLoaded = true;

        // Register commands
        new CommandLoader(this).loadCommands();
        new EventLoader(this).loadEvents();

        // Register services
        game.getServiceManager().setProvider(this, QuickStartUserService.class, configLoader);

        // Start tasks, save every thirty seconds
        game.getScheduler().createTaskBuilder().async().name("QuickStart Cleanup Task").delay(30, TimeUnit.SECONDS).interval(30, TimeUnit.SECONDS)
            .execute(() -> {
                this.getUserLoader().purgeNotOnline();
                this.configLoader.saveAll();
            }).submit(this);
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        if (!isErrored) {
            configLoader.saveAll();
        }
    }

    public Injector getInjector() {
        return injector;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getConfigDirPath() {
        return configDir;
    }

    public Path getDataPath() {
        return dataDir;
    }

    /**
     * Gets whether the modules are loaded.
     *
     * @return Whether the modules are loaded.
     */
    public boolean areModulesLoaded() {
        return this.modulesLoaded;
    }

    public UserConfigLoader getUserLoader() {
        return configLoader;
    }

    /**
     * Gets the configuration file
     *
     * @param config The {@link Class} of the config to get (see T).
     * @param <T> The type of {@link AbstractConfig} to get.
     * @return An {@link Optional} that might contain the config, if it exists.
     */
    public <T extends AbstractConfig> Optional<T> getConfig(Class<T> config) {
        return configMap.getConfig(config);
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public MailHandler getMailHandler() { return mailHandler; }

    public AFKHandler getAfkHandler() {
        return afkHandler;
    }
}

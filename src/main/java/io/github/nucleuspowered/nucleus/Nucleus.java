/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.exceptions.ModulesLoadedException;
import io.github.nucleuspowered.nucleus.api.exceptions.UnremovableModuleException;
import io.github.nucleuspowered.nucleus.api.service.*;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.config.MainConfig;
import io.github.nucleuspowered.nucleus.config.WarpsConfig;
import io.github.nucleuspowered.nucleus.config.bases.AbstractStandardNodeConfig;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.config.loaders.WorldConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.PluginSystemsLoader;
import io.github.nucleuspowered.nucleus.internal.guice.QuickStartInjectorModule;
import io.github.nucleuspowered.nucleus.internal.services.*;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import static io.github.nucleuspowered.nucleus.PluginInfo.*;

@Plugin(id = GROUP_ID, name = NAME, version = INFORMATIVE_VERSION, description = DESCRIPTION)
public class Nucleus {

    private ModuleRegistration moduleRegistration;
    private boolean modulesLoaded = false;
    private boolean isErrored = false;
    private final ConfigMap configMap = new ConfigMap();
    private UserConfigLoader configLoader;
    private WorldConfigLoader worldConfigLoader;
    private Injector injector;
    private MessageHandler messageHandler = new MessageHandler();
    private MailHandler mailHandler;
    private JailHandler jailHandler;
    private WarmupManager warmupManager;
    private TeleportHandler tpHandler = new TeleportHandler(this);
    private EconHelper econHelper = new EconHelper(this);
    private PermissionRegistry permissionRegistry = new PermissionRegistry();

    private AFKHandler afkHandler = new AFKHandler();

    @Inject private Game game;
    @Inject private Logger logger;
    private Path configDir;
    private Path dataDir;

    // We inject this into the constructor so we can build the config path ourselves.
    @Inject
    public Nucleus(@ConfigDir(sharedRoot = true) Path configDir) {
        this.configDir = configDir.resolve(PluginInfo.ID);
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitializationEvent) {
        logger.info(Util.getMessageWithFormat("startup.preinit", PluginInfo.NAME));

        dataDir = game.getSavesDirectory().resolve("nucleus");
        // Get the mandatory config files.
        try {
            Files.createDirectories(this.configDir);
            Files.createDirectories(dataDir);
            configMap.putConfig(ConfigMap.MAIN_CONFIG, new MainConfig(Paths.get(configDir.toString(), "main.conf")));
            configMap.putConfig(ConfigMap.COMMANDS_CONFIG, new CommandsConfig(Paths.get(configDir.toString(), "commands.conf")));
            configLoader = new UserConfigLoader(this);
            worldConfigLoader = new WorldConfigLoader(this);
            moduleRegistration = new ModuleRegistration(this);
            warmupManager = new WarmupManager();
        } catch (Exception e) {
            isErrored = true;
            e.printStackTrace();
            return;
        }

        // We register the ModuleService NOW so that others can hook into it.
        game.getServiceManager().setProvider(this, NucleusModuleService.class, moduleRegistration);
        game.getServiceManager().setProvider(this, NucleusWarmupManagerService.class, warmupManager);
        this.injector = Guice.createInjector(new QuickStartInjectorModule(this));
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        if (isErrored) {
            return;
        }

        logger.info(Util.getMessageWithFormat("startup.postinit", PluginInfo.NAME));
        Set<PluginModule> modules = moduleRegistration.getModulesToLoad();

        // Load the following services only if necessary.
        if (modules.contains(PluginModule.WARPS)) {
            try {
                configMap.putConfig(ConfigMap.WARPS_CONFIG, new WarpsConfig(Paths.get(dataDir.toString(), "warp.json")));

                // Put the warp service into the service manager.
                game.getServiceManager().setProvider(this, NucleusWarpService.class, configMap.getConfig(ConfigMap.WARPS_CONFIG).get());
            } catch (Exception ex) {
                try {
                    moduleRegistration.removeModule(PluginModule.WARPS, this);
                } catch (ModulesLoadedException | UnremovableModuleException e) {
                    // Nope.
                }

                logger.warn("Could not load the warp module for the reason below.");
                ex.printStackTrace();
            }
        }

        if (modules.contains(PluginModule.JAILS)) {
            try {
                configMap.putConfig(ConfigMap.JAILS_CONFIG, new WarpsConfig(Paths.get(dataDir.toString(), "jails.json")));

                jailHandler = new JailHandler(this);
                game.getServiceManager().setProvider(this, NucleusJailService.class, jailHandler);
            } catch (Exception ex) {
                try {
                    moduleRegistration.removeModule(PluginModule.JAILS, this);
                } catch (ModulesLoadedException | UnremovableModuleException e) {
                    // Nope.
                }

                logger.warn("Could not load the jail module for the reason below.");
                ex.printStackTrace();
            }
        }

        if (modules.contains(PluginModule.MAILS)) {
            mailHandler = new MailHandler(game, this);
            game.getServiceManager().setProvider(this, NucleusMailService.class, mailHandler);
        }

        if (modules.contains(PluginModule.KITS)) {
            try {
                KitsConfig config = new KitsConfig(Paths.get(dataDir.toString(), "kits.json"));
                configMap.putConfig(ConfigMap.KITS_CONFIG, config);
                game.getServiceManager().setProvider(this, NucleusKitService.class, config);
            } catch (Exception ex) {
                try {
                    moduleRegistration.removeModule(PluginModule.KITS, this);
                } catch (ModulesLoadedException | UnremovableModuleException e) {
                    // Nope.
                }

                logger.warn("Could not load the kits module for the reason below.");
                ex.printStackTrace();
            }
        }

        modulesLoaded = true;

        // Register commands, events and runnables.
        try {
            new PluginSystemsLoader(this).load();
        } catch (IOException e) {
            e.printStackTrace();
            isErrored = true;
            return;
        }

        // Register services
        game.getServiceManager().setProvider(this, NucleusUserLoaderService.class, configLoader);
        game.getServiceManager().setProvider(this, NucleusWorldLoaderService.class, worldConfigLoader);
        logger.info(Util.getMessageWithFormat("startup.started", PluginInfo.NAME));
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        if (!isErrored) {
            logger.info(Util.getMessageWithFormat("startup.stopped", PluginInfo.NAME));
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

    public WorldConfigLoader getWorldLoader() {
        return worldConfigLoader;
    }

    /**
     * Gets the configuration file
     *
     * @param key The {@link ConfigMap.Key} of the config to get (see T).
     * @param <T> The type of {@link AbstractStandardNodeConfig} to get.
     * @return An {@link Optional} that might contain the config, if it exists.
     */
    public <T extends AbstractStandardNodeConfig> Optional<T> getConfig(ConfigMap.Key<T> key) {
        return configMap.getConfig(key);
    }

    public void reload() {
        configMap.reloadAll();
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public MailHandler getMailHandler() { return mailHandler; }

    public AFKHandler getAfkHandler() {
        return afkHandler;
    }

    public JailHandler getJailHandler() {
        return jailHandler;
    }

    public WarmupManager getWarmupManager() {
        return warmupManager;
    }

    public TeleportHandler getTpHandler() {
        return tpHandler;
    }

    public EconHelper getEconHelper() {
        return econHelper;
    }

    public PermissionRegistry getPermissionRegistry() {
        return permissionRegistry;
    }
}

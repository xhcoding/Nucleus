/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserLoaderService;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarmupManagerService;
import io.github.nucleuspowered.nucleus.api.service.NucleusWorldLoaderService;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.config.configurate.NucleusObjectMapperFactory;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.config.loaders.WorldConfigLoader;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.guice.QuickStartInjectorModule;
import io.github.nucleuspowered.nucleus.internal.guice.QuickStartModuleLoaderInjector;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.qsml.ModuleRegistrationProxyService;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusLoggerProxy;
import io.github.nucleuspowered.nucleus.internal.qsml.QuickStartModuleConstructor;
import io.github.nucleuspowered.nucleus.internal.services.WarmupManager;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static io.github.nucleuspowered.nucleus.PluginInfo.*;

@Plugin(id = GROUP_ID, name = NAME, version = INFORMATIVE_VERSION, description = DESCRIPTION)
public class Nucleus {

    private boolean modulesLoaded = false;
    private boolean isErrored = false;
    private CommandsConfig commandsConfig;
    private GeneralDataStore generalDataStore;
    private UserConfigLoader configLoader;
    private WorldConfigLoader worldConfigLoader;
    private ChatUtil chatUtil;
    private Injector injector;

    private InternalServiceManager serviceManager = new InternalServiceManager();

    private WarmupManager warmupManager;
    private EconHelper econHelper = new EconHelper(this);
    private PermissionRegistry permissionRegistry = new PermissionRegistry();

    private ModuleContainer moduleContainer;

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
        ConfigurationLoader<CommentedConfigurationNode> cl;
        // Get the mandatory config files.
        try {
            Files.createDirectories(this.configDir);
            Files.createDirectories(dataDir);
            commandsConfig = new CommandsConfig(Paths.get(configDir.toString(), "commands.conf"));
            generalDataStore = new GeneralDataStore(Paths.get(dataDir.toString(), "general.json"));
            configLoader = new UserConfigLoader(this);
            worldConfigLoader = new WorldConfigLoader(this);
            warmupManager = new WarmupManager();
            chatUtil = new ChatUtil(configLoader);
            serviceManager.registerService(WarmupManager.class, warmupManager);
        } catch (Exception e) {
            isErrored = true;
            e.printStackTrace();
            return;
        }

        // We register the ModuleService NOW so that others can hook into it.
        game.getServiceManager().setProvider(this, NucleusModuleService.class, new ModuleRegistrationProxyService(this));
        game.getServiceManager().setProvider(this, NucleusWarmupManagerService.class, warmupManager);
        this.injector = Guice.createInjector(new QuickStartInjectorModule(this));
        Injector qsmlInjector = Guice.createInjector(new QuickStartModuleLoaderInjector(this));

        try {
            moduleContainer = ModuleContainer.builder()
                    .setConstructor(new QuickStartModuleConstructor(qsmlInjector))
                    .setConfigurationLoader(HoconConfigurationLoader.builder()
                            .setDefaultOptions(ConfigurationOptions.defaults().setObjectMapperFactory(NucleusObjectMapperFactory.getInstance()))
                            .setPath(Paths.get(configDir.toString(), "main.conf"))
                            .build())
                    .setPackageToScan(getClass().getPackage().getName() + ".modules")
                    .setLoggerProxy(new NucleusLoggerProxy(logger))
                    .build();
        } catch (QuickStartModuleDiscoveryException e) {
            isErrored = true;
            e.printStackTrace();
        }
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        if (isErrored) {
            return;
        }

        logger.info(Util.getMessageWithFormat("startup.postinit", PluginInfo.NAME));
        try {
            logger.info(Util.getMessageWithFormat("startup.moduleloading", PluginInfo.NAME));
            moduleContainer.loadModules(false);
        } catch (QuickStartModuleLoaderException.Construction | QuickStartModuleLoaderException.Enabling construction) {
            logger.info(Util.getMessageWithFormat("startup.modulenotloaded", PluginInfo.NAME));
            construction.printStackTrace();
            isErrored = true;
            return;
        }

        logger.info(Util.getMessageWithFormat("startup.moduleloaded", PluginInfo.NAME));
        registerPermissions();
        modulesLoaded = true;

        // Register final services
        game.getServiceManager().setProvider(this, NucleusUserLoaderService.class, configLoader);
        game.getServiceManager().setProvider(this, NucleusWorldLoaderService.class, worldConfigLoader);
        logger.info(Util.getMessageWithFormat("startup.started", PluginInfo.NAME));
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        if (!isErrored) {
            logger.info(Util.getMessageWithFormat("startup.stopped", PluginInfo.NAME));
            saveData();
        }
    }

    public void saveData() {
        configLoader.saveAll();
        worldConfigLoader.saveAll();
        try {
            generalDataStore.save();
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
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

    public void reload() {
        try {
            moduleContainer.reloadSystemConfig();
            commandsConfig.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WarmupManager getWarmupManager() {
        return warmupManager;
    }

    public EconHelper getEconHelper() {
        return econHelper;
    }

    public PermissionRegistry getPermissionRegistry() {
        return permissionRegistry;
    }

    public ModuleContainer getModuleContainer() {
        return moduleContainer;
    }

    public InternalServiceManager getInternalServiceManager() {
        return serviceManager;
    }

    public GeneralDataStore getGeneralDataStore() {
        return generalDataStore;
    }

    public CommandsConfig getCommandsConfig() {
        return commandsConfig;
    }

    public ChatUtil getChatUtil() {
        return chatUtil;
    }

    private void registerPermissions() {
        Optional<PermissionService> ops = Sponge.getServiceManager().provide(PermissionService.class);
        if (ops.isPresent()) {
            Optional<PermissionDescription.Builder> opdb = ops.get().newDescriptionBuilder(this);
            if (opdb.isPresent()) {
                Map<String, PermissionInformation> m = this.getPermissionRegistry().getPermissions();
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.ADMIN).forEach(k -> ops.get().newDescriptionBuilder(this).get().assign(PermissionDescription.ROLE_ADMIN, true).description(k.getValue().description).id(k.getKey()).register());
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.MOD).forEach(k -> ops.get().newDescriptionBuilder(this).get().assign(PermissionDescription.ROLE_STAFF, true).description(k.getValue().description).id(k.getKey()).register());
                m.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.USER).forEach(k -> ops.get().newDescriptionBuilder(this).get().assign(PermissionDescription.ROLE_USER, true).description(k.getValue().description).id(k.getKey()).register());
            }
        }
    }
}

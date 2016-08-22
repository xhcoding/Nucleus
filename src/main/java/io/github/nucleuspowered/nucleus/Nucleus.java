/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserLoaderService;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarmupManagerService;
import io.github.nucleuspowered.nucleus.api.service.NucleusWorldLoaderService;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.config.MessageConfig;
import io.github.nucleuspowered.nucleus.configurate.objectmapper.NucleusObjectMapperFactory;
import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProviders;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import io.github.nucleuspowered.nucleus.internal.guice.QuickStartInjectorModule;
import io.github.nucleuspowered.nucleus.internal.guice.SubInjectorModule;
import io.github.nucleuspowered.nucleus.internal.messages.ConfigMessageProvider;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.messages.ResourceMessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.qsml.ModuleRegistrationProxyService;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusLoggerProxy;
import io.github.nucleuspowered.nucleus.internal.qsml.QuickStartModuleConstructor;
import io.github.nucleuspowered.nucleus.internal.qsml.event.BaseModuleEvent;
import io.github.nucleuspowered.nucleus.internal.services.WarmupManager;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.events.NucleusReloadConfigEvent;
import io.github.nucleuspowered.nucleus.util.ThrowableAction;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.modulecontainers.DiscoveryModuleContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.nucleuspowered.nucleus.PluginInfo.*;

@Plugin(id = ID, name = NAME, version = VERSION, description = DESCRIPTION)
public class Nucleus {

    private boolean modulesLoaded = false;
    private boolean isErrored = false;
    private CommandsConfig commandsConfig;
    private GeneralService generalService;
    private UserDataManager userDataManager;
    private WorldDataManager worldDataManager;
    private ChatUtil chatUtil;
    private Injector injector;
    private SubInjectorModule subInjectorModule = new SubInjectorModule();
    private List<ThrowableAction<? extends Exception>> reloadableList = Lists.newArrayList();

    private InternalServiceManager serviceManager = new InternalServiceManager(this);
    private MessageProvider messageProvider = new ResourceMessageProvider();

    private WarmupManager warmupManager;
    private EconHelper econHelper = new EconHelper(this);
    private PermissionRegistry permissionRegistry = new PermissionRegistry();

    private DiscoveryModuleContainer moduleContainer;

    private final Map<String, TextFileController> textFileControllers = Maps.newHashMap();

    @Inject private Game game;
    @Inject private Logger logger;
    private Path configDir;
    private Path dataDir;

    // We inject this into the constructor so we can build the config path ourselves.
    @Inject
    public Nucleus(@ConfigDir(sharedRoot = true) Path configDir) {
        this.configDir = configDir.resolve(PluginInfo.ID);
        Util.setMessageProvider(() -> messageProvider);
        NameUtil.supplyPlugin(this);
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitializationEvent) {
        logger.info(messageProvider.getMessageWithFormat("startup.preinit", PluginInfo.NAME));

        dataDir = game.getSavesDirectory().resolve("nucleus");
        // Get the mandatory config files.
        try {
            Files.createDirectories(this.configDir);
            Files.createDirectories(dataDir);
            commandsConfig = new CommandsConfig(Paths.get(configDir.toString(), "commands.conf"));

            DataProviders d = new DataProviders(this);
            generalService = new GeneralService(d.getGeneralDataProvider());
            userDataManager = new UserDataManager(this, d::getUserFileDataProviders);
            worldDataManager = new WorldDataManager(this, d::getWorldFileDataProvider);
            warmupManager = new WarmupManager();
            chatUtil = new ChatUtil(this);
        } catch (Exception e) {
            isErrored = true;
            e.printStackTrace();
            return;
        }

        // We register the ModuleService NOW so that others can hook into it.
        game.getServiceManager().setProvider(this, NucleusModuleService.class, new ModuleRegistrationProxyService(this));
        game.getServiceManager().setProvider(this, NucleusWarmupManagerService.class, warmupManager);
        this.injector = Guice.createInjector(new QuickStartInjectorModule(this));
        serviceManager.registerService(WarmupManager.class, warmupManager);

        try {
            moduleContainer = DiscoveryModuleContainer.builder()
                    .setConstructor(new QuickStartModuleConstructor(injector))
                    .setConfigurationLoader(HoconConfigurationLoader.builder()
                            .setDefaultOptions(ConfigurationOptions.defaults().setObjectMapperFactory(NucleusObjectMapperFactory.getInstance()))
                            .setPath(Paths.get(configDir.toString(), "main.conf"))
                            .build())
                    .setPackageToScan(getClass().getPackage().getName() + ".modules")
                    .setLoggerProxy(new NucleusLoggerProxy(logger))
                    .setOnPreEnable(() -> {
                        runInjectorUpdate();
                        Sponge.getEventManager().post(new BaseModuleEvent.AboutToEnable(this));
                    })
                    .setOnEnable(() -> {
                        runInjectorUpdate();
                        Sponge.getEventManager().post(new BaseModuleEvent.PreEnable(this));
                    })
                    .setOnPostEnable(() -> {
                        Sponge.getEventManager().post(new BaseModuleEvent.Enabled(this));
                    })
                    .build();

            moduleContainer.startDiscover();
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

        logger.info(messageProvider.getMessageWithFormat("startup.postinit", PluginInfo.NAME));

        // Load up the general data file now, mods should have registered items by now.
        try {
            generalService.load();
        } catch (Exception e) {
            isErrored = true;
            e.printStackTrace();
            return;
        }

        try {
            Sponge.getEventManager().post(new BaseModuleEvent.AboutToConstructEvent(this));
            logger.info(messageProvider.getMessageWithFormat("startup.moduleloading", PluginInfo.NAME));
            moduleContainer.loadModules(false);
        } catch (QuickStartModuleLoaderException.Construction | QuickStartModuleLoaderException.Enabling construction) {
            logger.info(messageProvider.getMessageWithFormat("startup.modulenotloaded", PluginInfo.NAME));
            construction.printStackTrace();
            isErrored = true;
            return;
        }

        logger.info(messageProvider.getMessageWithFormat("startup.moduleloaded", PluginInfo.NAME));
        registerPermissions();
        modulesLoaded = true;
        Sponge.getEventManager().post(new BaseModuleEvent.Complete(this));

        // Register final services
        game.getServiceManager().setProvider(this, NucleusUserLoaderService.class, userDataManager);
        game.getServiceManager().setProvider(this, NucleusWorldLoaderService.class, worldDataManager);
        logger.info(messageProvider.getMessageWithFormat("startup.started", PluginInfo.NAME));
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        if (!isErrored) {
            logger.info(messageProvider.getMessageWithFormat("startup.stopped", PluginInfo.NAME));
            saveData();
        }
    }

    public void saveData() {
        userDataManager.saveAll();
        worldDataManager.saveAll();
        try {
            generalService.save();
        } catch (Exception e) {
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

    public UserDataManager getUserDataManager() {
        return userDataManager;
    }

    public WorldDataManager getWorldDataManager() {
        return worldDataManager;
    }

    public void saveSystemConfig() throws IOException {
        moduleContainer.saveSystemConfig();
    }

    public void reload() {
        try {
            moduleContainer.reloadSystemConfig();
            reloadMessages();
            commandsConfig.load();

            for (TextFileController tfc : textFileControllers.values()) {
                tfc.load();
            }

            for (ThrowableAction<? extends Exception> r : reloadableList) {
                r.action();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Sponge.getEventManager().post(new NucleusReloadConfigEvent(this));
    }

    public void reloadMessages() {
        try {
            if (moduleContainer.getConfigAdapterForModule("core", CoreConfigAdapter.class).getNodeOrDefault().isCustommessages()) {
                this.messageProvider = new ConfigMessageProvider(new MessageConfig(configDir.resolve("messages.conf")));
            } else {
                this.messageProvider = new ResourceMessageProvider();
            }
        } catch (Exception e) {
            // On error, fallback.
            this.messageProvider = new ResourceMessageProvider();
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

    public DiscoveryModuleContainer getModuleContainer() {
        return moduleContainer;
    }

    public InternalServiceManager getInternalServiceManager() {
        return serviceManager;
    }

    public GeneralService getGeneralService() {
        return generalService;
    }

    public CommandsConfig getCommandsConfig() {
        return commandsConfig;
    }

    public ChatUtil getChatUtil() {
        return chatUtil;
    }

    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    public <T> void preInjectorUpdate(Class<T> clazz, T instance) {
        if (injector.getExistingBinding(Key.get(clazz)) == null) {
            subInjectorModule.addInjection(clazz, instance);
        } else {
            logger.warn(Util.getMessageWithFormat("nucleus.injector.duplicate", clazz.getName()));
        }
    }

    /**
     * Gets the {@link TextFileController}
     *
     * @param getController The ID of the {@link TextFileController}.
     * @return An {@link Optional} that might contain a {@link TextFileController}.
     */
    public Optional<TextFileController> getTextFileController(String getController) {
        return Optional.ofNullable(textFileControllers.get(getController));
    }

    public void addTextFileController(String id, Asset asset, Path file) throws IOException {
        if (!textFileControllers.containsKey(id)) {
            textFileControllers.put(id, new TextFileController(asset, file));
        }
    }

    public void registerReloadable(ThrowableAction<? extends Exception> reloadable) {
        reloadableList.add(reloadable);
    }

    private Injector runInjectorUpdate() {
        if (subInjectorModule.isEmpty()) {
            return injector;
        }

        injector = injector.createChildInjector(subInjectorModule);
        subInjectorModule = new SubInjectorModule();
        return injector;
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

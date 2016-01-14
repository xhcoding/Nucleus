package uk.co.drnaylor.minecraft.quickstart;

import com.google.common.reflect.TypeToken;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartUserService;
import uk.co.drnaylor.minecraft.quickstart.config.AbstractConfig;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandLoader;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.EventLoader;
import uk.co.drnaylor.minecraft.quickstart.internal.services.ModuleRegistration;
import uk.co.drnaylor.minecraft.quickstart.internal.guice.QuickStartInjectorModule;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = QuickStart.ID, name = QuickStart.NAME, version = QuickStart.VERSION)
public class QuickStart {
    public final static String ID = "quickstart";
    public final static String NAME = "Quick Start";
    public final static String VERSION = "0.1";
    public final static String PERMISSIONS_PREFIX = "quickstart.";
    public final static String PERMISSIONS_ADMIN = PERMISSIONS_PREFIX + "admin";
    public final static Text MESSAGE_PREFIX = Text.of(TextColors.GREEN, "[" + NAME + "]");
    public final static Text ERROR_MESSAGE_PREFIX = Text.of(TextColors.RED, "[" + NAME + "]");

    private boolean modulesLoaded = false;
    private boolean isErrored = false;
    private final ConfigMap configMap = new ConfigMap();
    private final UserConfigLoader configLoader = new UserConfigLoader(this);
    private Injector injector;

    @Inject private Game game;
    @Inject private Logger logger;
    @Inject @DefaultConfig(sharedRoot = false) private Path path;

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitializationEvent) {
        this.injector = Guice.createInjector(new QuickStartInjectorModule(this));

        // Get the config file.
        try {
            configMap.putConfig(new MainConfig(path));
        } catch (IOException e) {
            isErrored = true;
            e.printStackTrace();
            return;
        }

        // We register the ModuleService NOW so that others can hook into it.
        game.getServiceManager().setProvider(this, QuickStartModuleService.class, new ModuleRegistration(this));
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        if (isErrored) {
            return;
        }

        modulesLoaded = true;

        // Register commands
        new CommandLoader(this).loadCommands();
        new EventLoader(this).loadEvents();

        // Register services
        game.getServiceManager().setProvider(this, QuickStartUserService.class, configLoader);
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

    /**
     * Gets whether the modules are loaded.
     *
     * @return Whether the modules are loaded.
     */
    public boolean areModulesLoaded() {
        return this.modulesLoaded;
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
}

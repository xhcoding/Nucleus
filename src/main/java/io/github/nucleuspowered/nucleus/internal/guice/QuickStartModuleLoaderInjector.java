/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.guice;

import com.google.inject.AbstractModule;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.config.loaders.WorldConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.ModuleContainer;

public class QuickStartModuleLoaderInjector extends AbstractModule {

    protected final Nucleus plugin;
    private final ConfigMap configMap;

    public QuickStartModuleLoaderInjector(Nucleus plugin, ConfigMap configMap) {
        this.plugin = plugin;
        this.configMap = configMap;
    }

    @Override
    protected void configure() {
        bind(Nucleus.class).toProvider(() -> plugin);
        bind(Logger.class).toProvider(plugin::getLogger);
        bind(CommandsConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.COMMANDS_CONFIG).get());
        bind(UserConfigLoader.class).toProvider(plugin::getUserLoader);
        bind(WorldConfigLoader.class).toProvider(plugin::getWorldLoader);
        bind(Game.class).toProvider(Sponge::getGame);
        bind(PermissionRegistry.class).toProvider(plugin::getPermissionRegistry);
        bind(EconHelper.class).toProvider(plugin::getEconHelper);
        bind(ModuleContainer.class).toProvider(plugin::getModuleContainer);
        bind(InternalServiceManager.class).toProvider(plugin::getInternalServiceManager);
        bind(ConfigMap.class).toProvider(() -> configMap);
    }
}

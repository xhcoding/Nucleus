/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.guice;

import com.google.inject.AbstractModule;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.config.MainConfig;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.services.JailHandler;
import io.github.nucleuspowered.nucleus.internal.services.MailHandler;
import io.github.nucleuspowered.nucleus.internal.services.TeleportHandler;
import io.github.nucleuspowered.nucleus.internal.services.WarmupManager;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.services.datastore.WorldConfigLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;

public class QuickStartInjectorModule extends AbstractModule {
    private final Nucleus plugin;

    public QuickStartInjectorModule(Nucleus plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Nucleus.class).toProvider(() -> plugin);
        bind(Logger.class).toProvider(plugin::getLogger);
        bind(MainConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.MAIN_CONFIG).get());
        bind(CommandsConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.COMMANDS_CONFIG).get());
        bind(KitsConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.KITS_CONFIG).get());
        bind(UserConfigLoader.class).toProvider(plugin::getUserLoader);
        bind(WorldConfigLoader.class).toProvider(plugin::getWorldLoader);
        bind(Game.class).toProvider(Sponge::getGame);
        bind(MailHandler.class).toProvider(plugin::getMailHandler);
        bind(JailHandler.class).toProvider(plugin::getJailHandler);
        bind(WarmupManager.class).toProvider(plugin::getWarmupManager);
        bind(TeleportHandler.class).toProvider(plugin::getTpHandler);
        bind(PermissionRegistry.class).toProvider(plugin::getPermissionRegistry);
    }
}

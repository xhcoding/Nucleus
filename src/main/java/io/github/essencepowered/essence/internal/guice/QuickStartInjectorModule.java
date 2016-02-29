/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal.guice;

import com.google.inject.AbstractModule;
import io.github.essencepowered.essence.Essence;
import io.github.essencepowered.essence.config.CommandsConfig;
import io.github.essencepowered.essence.config.MainConfig;
import io.github.essencepowered.essence.internal.ConfigMap;
import io.github.essencepowered.essence.internal.PermissionRegistry;
import io.github.essencepowered.essence.internal.services.*;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;

public class QuickStartInjectorModule extends AbstractModule {
    private final Essence plugin;
    private final UserConfigLoader loader;

    public QuickStartInjectorModule(Essence plugin, UserConfigLoader configLoader) {
        this.plugin = plugin;
        this.loader = configLoader;
    }

    @Override
    protected void configure() {
        bind(Essence.class).toProvider(() -> plugin);
        bind(Logger.class).toProvider(plugin::getLogger);
        bind(MainConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.MAIN_CONFIG).get());
        bind(CommandsConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.COMMANDS_CONFIG).get());
        bind(UserConfigLoader.class).toProvider(() -> loader);
        bind(Game.class).toProvider(Sponge::getGame);
        bind(MailHandler.class).toProvider(plugin::getMailHandler);
        bind(JailHandler.class).toProvider(plugin::getJailHandler);
        bind(WarmupManager.class).toProvider(plugin::getWarmupManager);
        bind(TeleportHandler.class).toProvider(plugin::getTpHandler);
        bind(PermissionRegistry.class).toProvider(plugin::getPermissionRegistry);
    }
}

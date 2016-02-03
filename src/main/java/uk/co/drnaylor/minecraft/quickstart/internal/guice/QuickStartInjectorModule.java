package uk.co.drnaylor.minecraft.quickstart.internal.guice;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.services.JailHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.services.MailHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

public class QuickStartInjectorModule extends AbstractModule {
    private final QuickStart plugin;
    private final UserConfigLoader loader;

    public QuickStartInjectorModule(QuickStart plugin, UserConfigLoader configLoader) {
        this.plugin = plugin;
        this.loader = configLoader;
    }

    @Override
    protected void configure() {
        bind(QuickStart.class).toProvider(() -> plugin);
        bind(Logger.class).toProvider(plugin::getLogger);
        bind(MainConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.MAIN_CONFIG).get());
        bind(CommandsConfig.class).toProvider(() -> plugin.getConfig(ConfigMap.COMMANDS_CONFIG).get());
        bind(UserConfigLoader.class).toProvider(() -> loader);
        bind(Game.class).toProvider(Sponge::getGame);
        bind(MailHandler.class).toProvider(plugin::getMailHandler);
        bind(JailHandler.class).toProvider(plugin::getJailHandler);
    }
}

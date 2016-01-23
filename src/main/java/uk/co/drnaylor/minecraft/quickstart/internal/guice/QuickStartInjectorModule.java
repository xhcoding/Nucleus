package uk.co.drnaylor.minecraft.quickstart.internal.guice;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
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
        bind(QuickStart.class).toInstance(plugin);
        bind(Logger.class).toInstance(plugin.getLogger());
        bind(CommandsConfig.class).toInstance(plugin.getConfig(CommandsConfig.class).get());
        bind(UserConfigLoader.class).toInstance(loader);
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.util;

import com.google.inject.AbstractModule;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.config.MainConfig;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        Path test;
        Path test2;
        try {
            test = Files.createTempDirectory("quick");
            test2 = Files.createTempFile(test, "quick", "conf");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        this.bind(Path.class).annotatedWith(DefaultConfig.class).toInstance(test2);
        this.bind(Path.class).annotatedWith(ConfigDir.class).toInstance(test);
        this.bind(Game.class).toInstance(Mockito.mock(Game.class));
        this.bind(Logger.class).toInstance(Mockito.mock(Logger.class));
        this.bind(MainConfig.class).toInstance(Mockito.mock(MainConfig.class));
        this.bind(Nucleus.class).toInstance(getMockPlugin());
    }

    private Nucleus getMockPlugin() {
        Nucleus plugin = Mockito.mock(Nucleus.class);
        PermissionRegistry pr = new PermissionRegistry();
        Mockito.when(plugin.getPermissionRegistry()).thenReturn(pr);
        try {
            Path file = Files.createTempFile("quickstartcmdtest", "conf");
            CommandsConfig cc = new CommandsConfig(file);
            Mockito.when(plugin.getConfig(ConfigMap.COMMANDS_CONFIG)).thenReturn(Optional.of(cc));
            return plugin;
        } catch (IOException | ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }
}

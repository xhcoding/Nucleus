/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.migrators;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularWorldService;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Defines the migrator method.
 */
public abstract class DataMigrator {

    @Inject protected NucleusPlugin plugin;
    @Inject protected Logger logger;

    /**
     * Migrates data to NucleusPlugin from a data source.
     *
     * @param src The {@link CommandSource} that requested the migration.
     * @throws Exception Any injections.
     */
    public abstract void migrate(CommandSource src) throws Exception;

    protected final Optional<ModularUserService> getUser(UUID uuid) {
        try {
            return plugin.getUserDataManager().get(uuid);
        } catch (Exception e) {
            plugin.getLogger().warn("command.migrate.user.noexist", uuid.toString());
            return Optional.empty();
        }
    }

    protected final Optional<ModularWorldService> getWorld(UUID uuid) {
        try {
            return plugin.getWorldDataManager().get(uuid);
        } catch (Exception e) {
            plugin.getLogger().warn("command.migrate.world.noexist", uuid.toString());
            return Optional.empty();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface PluginDependency {

        /**
         * The IDs of any dependencies.
         *
         * @return The dependencies.
         */
        String[] value() default {};
    }
}

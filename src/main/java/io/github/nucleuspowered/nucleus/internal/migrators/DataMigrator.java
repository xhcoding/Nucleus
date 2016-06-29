/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.migrators;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.data.NucleusWorld;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;

import javax.inject.Inject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines the migrator method.
 */
public abstract class DataMigrator {

    @Inject protected Nucleus plugin;
    @Inject protected Logger logger;

    /**
     * Migrates data to Nucleus from a data source.
     *
     * @param src The {@link CommandSource} that requested the migration.
     * @throws Exception Any injections.
     */
    public abstract void migrate(CommandSource src) throws Exception;

    protected final Optional<InternalNucleusUser> getUser(UUID uuid) {
        try {
            return Optional.of(plugin.getUserLoader().getUser(uuid));
        } catch (Exception e) {
            plugin.getLogger().warn("command.migrate.user.noexist", uuid.toString());
            return Optional.empty();
        }
    }

    protected final Optional<NucleusWorld> getWorld(UUID uuid) {
        try {
            return Optional.of(plugin.getWorldLoader().getWorld(uuid));
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

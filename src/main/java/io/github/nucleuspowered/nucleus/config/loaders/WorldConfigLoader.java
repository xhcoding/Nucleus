/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.loaders;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.data.NucleusWorld;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchWorldException;
import io.github.nucleuspowered.nucleus.api.service.NucleusWorldLoaderService;
import io.github.nucleuspowered.nucleus.config.WorldService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Loader that loads configuration files for worlds.
 */
public class WorldConfigLoader extends AbstractDataLoader<UUID, WorldService> implements NucleusWorldLoaderService {

    public WorldConfigLoader(Nucleus plugin) {
        super(plugin);
    }

    @Override
    public NucleusWorld getWorld(UUID uuid) throws Exception {
        return getWorld(Sponge.getServer().getWorld(uuid).orElseThrow(NoSuchWorldException::new));
    }

    @Override
    public NucleusWorld getWorld(World world) throws Exception {
        Optional<WorldService> ows = get(world.getUniqueId());
        if (ows.isPresent()) {
            return ows.get();
        }

        // Load the file in.
        WorldService uc = new WorldService(plugin, getWorldPath(world.getUniqueId()), world);
        loaded.put(world.getUniqueId(), uc);
        return uc;
    }

    public Path getWorldPath(UUID uuid) throws IOException {
        String u = uuid.toString();
        String f = u.substring(0, 2);
        Path file = plugin.getDataPath().resolve(String.format("worlddata%1$s%2$s%1$s%3$s.json", File.separator, f, u));

        if (Files.notExists(file)) {
            Files.createDirectories(file.getParent());
        }

        // Configurate will create it for us.
        return file;
    }
}

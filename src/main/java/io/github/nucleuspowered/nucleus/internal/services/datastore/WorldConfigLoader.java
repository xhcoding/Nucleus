/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services.datastore;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.data.NucleusWorld;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchWorldException;
import io.github.nucleuspowered.nucleus.api.service.NucleusWorldLoaderService;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * Loader that loads configuration files for worlds.
 */
public class WorldConfigLoader implements NucleusWorldLoaderService {

    private final Nucleus plugin;
    private final Map<UUID, WorldService> loaded = Maps.newHashMap();

    public WorldConfigLoader(Nucleus plugin) {
        this.plugin = plugin;
    }

    @Override
    public NucleusWorld getWorld(UUID uuid) throws NoSuchWorldException, IOException, ObjectMappingException {
        return getWorld(Sponge.getServer().getWorld(uuid).orElseThrow(NoSuchWorldException::new));
    }

    @Override
    public NucleusWorld getWorld(World world) throws IOException, ObjectMappingException {
        if (loaded.containsKey(world.getUniqueId())) {
            return loaded.get(world.getUniqueId());
        }

        // Load the file in.
        WorldService uc = new WorldService(plugin, getWorldPath(world.getUniqueId()), world);
        loaded.put(world.getUniqueId(), uc);
        return uc;
    }

    @Override
    public void saveAll() {
        loaded.values().forEach(c -> {
            try {
                c.save();
            } catch (IOException | ObjectMappingException e) {
                plugin.getLogger().error("Could not save data for " + c.getUniqueID().toString());
                e.printStackTrace();
            }
        });
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

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.configurate.datatypes.WorldDataNode;
import io.github.nucleuspowered.nucleus.dataservices.WorldService;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.iapi.data.NucleusWorld;
import io.github.nucleuspowered.nucleus.iapi.service.NucleusWorldLoaderService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class WorldDataManager extends DataManager<UUID, WorldDataNode, WorldService> implements NucleusWorldLoaderService {

    public WorldDataManager(NucleusPlugin plugin, Function<UUID, DataProvider<WorldDataNode>> dataProviderFactory, Predicate<UUID> fileExist) {
        super(plugin, dataProviderFactory, fileExist);
    }

    @Override
    public Optional<WorldService> getNew(UUID data, DataProvider<WorldDataNode> dataProvider) throws Exception {
        return Optional.of(new WorldService(plugin, dataProvider, Sponge.getServer().getWorldProperties(data).orElseThrow(() -> new IllegalStateException("world"))));
    }

    @Override
    public Optional<NucleusWorld> getWorld(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        return Optional.ofNullable(get(uuid).orElse(null));
    }

    @Override
    public Optional<NucleusWorld> getWorld(World world) {
        Preconditions.checkNotNull(world);
        return getWorld(world.getUniqueId());
    }
}

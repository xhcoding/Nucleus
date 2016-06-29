/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.loaders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.config.bases.AbstractConfig;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDataLoader<K, D extends AbstractConfig<?, ?>> {

    private boolean isSaving = false;

    protected final Nucleus plugin;
    protected final Map<K, D> loaded = Maps.newHashMap();

    protected AbstractDataLoader(Nucleus plugin) {
        this.plugin = plugin;
    }

    public Optional<D> get(K key) {
        return Optional.ofNullable(loaded.get(key));
    }

    public void saveAll() {
        if (isSaving) {
            return;
        }

        isSaving = true;
        // Copy list so that we can save the items now, and avoid CMEs on this list.
        Lists.newArrayList(loaded.values()).forEach(c -> {
            try {
                c.save();
            } catch (IOException | ObjectMappingException e) {
                plugin.getLogger().error("Could not save data for " + c.toString());
                e.printStackTrace();
            }
        });

        isSaving = false;
    }
}

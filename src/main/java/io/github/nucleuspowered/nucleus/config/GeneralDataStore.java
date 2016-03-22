/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.config.bases.AbstractSerialisableClassConfig;
import io.github.nucleuspowered.nucleus.config.serialisers.GeneralDataNode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.item.ItemType;

import java.nio.file.Path;
import java.util.List;

public class GeneralDataStore extends AbstractSerialisableClassConfig<GeneralDataNode, ConfigurationNode, ConfigurationLoader<ConfigurationNode>> {

    public GeneralDataStore(Path file) throws Exception {
        super(file, TypeToken.of(GeneralDataNode.class), GeneralDataNode::new);
    }

    @Override
    protected ConfigurationNode getNode() {
        return SimpleConfigurationNode.root();
    }

    @Override
    protected ConfigurationLoader<ConfigurationNode> getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    public List<ItemType> getBlacklistedTypes() {
        return ImmutableList.copyOf(data.getBlacklistedTypes());
    }

    public boolean addBlacklistedType(ItemType type) {
        List<ItemType> types = data.getBlacklistedTypes();
        if (!types.contains(type)) {
            types.add(type);
            return true;
        }

        return false;
    }

    public boolean removeBlacklistedType(ItemType type) {
        return data.getBlacklistedTypes().remove(type);
    }
}

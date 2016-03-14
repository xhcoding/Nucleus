/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.data.Kit;
import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import io.github.nucleuspowered.nucleus.config.bases.AbstractStandardNodeConfig;
import io.github.nucleuspowered.nucleus.config.serialisers.KitDataNode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class KitsConfig extends AbstractStandardNodeConfig<ConfigurationNode, GsonConfigurationLoader> implements NucleusKitService {

    @Inject private Logger logger;

    private Map<String, KitDataNode> kitNodes;

    public KitsConfig(Path file) throws Exception {
        super(file);
    }

    @Override
    public void load() throws IOException, ObjectMappingException {
        node = loader.load();

        if (kitNodes == null) {
            kitNodes = Maps.newHashMap();
        }

        kitNodes.clear();

        node.getChildrenMap().forEach((k, v) -> {
            try {
                kitNodes.put(String.valueOf(k), v.getValue(TypeToken.of(KitDataNode.class), new KitDataNode()));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });
    }

    @Override
    public void save() throws IOException, ObjectMappingException {
        node = SimpleCommentedConfigurationNode.root();
        kitNodes.forEach((k, v) -> {
            try {
                node.getNode(k).setValue(TypeToken.of(KitDataNode.class), v);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });
        super.save();
    }

    @Override
    protected GsonConfigurationLoader getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected ConfigurationNode getDefaults() {
        return SimpleConfigurationNode.root();
    }

    @Override
    public Set<String> getKitNames() {
        return kitNodes.keySet();
    }

    @Override
    public Optional<Kit> getKit(String name) {
        return Optional.ofNullable(kitNodes.get(name));
    }

    @Override
    public void removeKit(String kitName) {
        kitNodes.remove(kitName);
    }

    @Override
    public void saveKit(String kitName, Kit kit) {
        Preconditions.checkNotNull(kit);
        kitNodes.put(kitName, (KitDataNode) kit);
    }

    @Override
    public Kit createKit() {
        return new KitDataNode();
    }
}

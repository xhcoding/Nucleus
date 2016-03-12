/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.config.serialisers.Kit;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KitsConfig extends AbstractConfig<ConfigurationNode, GsonConfigurationLoader> {

    @Inject private Logger logger;

    private Map<String, Kit> kitNodes;

    public KitsConfig(Path file) throws IOException, ObjectMappingException {
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
                kitNodes.put(String.valueOf(k), v.getValue(TypeToken.of(Kit.class), new Kit()));
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
                node.getNode(k).setValue(TypeToken.of(Kit.class), v);
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

    public Set<String> getKits() {
        return kitNodes.keySet();
    }

    public Kit getKit(String name) {
        return kitNodes.get(name);
    }

    public void removeKit(String kitName) {
        kitNodes.remove(kitName);
    }

    public void saveInventoryAsKit(Player player, String kitName) {
        List<Inventory> slots = Lists.newArrayList(player.getInventory().slots());
        final List<ItemStack> stacks = Lists.newArrayList();

        // Add all the stacks into the kit list.
        slots.forEach(s -> s.peek().ifPresent(stacks::add));

        Kit kitInventory = new Kit(stacks);
        kitNodes.put(kitName, kitInventory);
    }

    public void setInterval(String kitName, Duration interval) {
        this.kitNodes.get(kitName).setInterval(interval);
    }
}

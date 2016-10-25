/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.typeserialisers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;
import java.util.Optional;

public class ItemStackSnapshotSerialiser implements TypeSerializer<ItemStackSnapshot> {

    public static final ItemStackSnapshotSerialiser INSTANCE = new ItemStackSnapshotSerialiser();
    private static final TypeToken<ItemStackSnapshot> tt = TypeToken.of(ItemStackSnapshot.class);

    public List<ItemStackSnapshot> deserializeList(List<ConfigurationNode> stacks) {
        List<ItemStackSnapshot> lcn = Lists.newArrayList();
        for (ConfigurationNode cn : stacks) {
            try {
                lcn.add(deserialize(tt, cn));
            } catch (ObjectMappingException e) {
                //
            }
        }

        return lcn;
    }

    public List<ConfigurationNode> serializeList(List<ItemStackSnapshot> liss) {
        List<ConfigurationNode> stacks = Lists.newArrayList();
        for (ItemStackSnapshot stackSnapshot : liss) {
            ConfigurationNode cn = SimpleConfigurationNode.root();
            try {
                serialize(tt, stackSnapshot, cn);
                stacks.add(cn);
            } catch (ObjectMappingException e) {
                //
            }
        }

        return stacks;
    }

    @Override
    public ItemStackSnapshot deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        // Process enchantments, temporary fix before Sponge gets a more general fix in.
        ConfigurationNode ench = value.getNode("UnsafeData", "ench");
        if (!ench.isVirtual()) {
            List<? extends ConfigurationNode> enchantments = ench.getChildrenList();
            if (enchantments.isEmpty()) {
                // Remove empty enchantment list.
                value.getNode("UnsafeData").removeChild("ench");
            } else {
                enchantments.forEach(x -> {
                    try {
                        int id = Integer.parseInt(x.getNode("id").getString());
                        int lvl = Integer.parseInt(x.getNode("lvl").getString());

                        x.getNode("id").setValue(id);
                        x.getNode("lvl").setValue(lvl);
                    } catch (NumberFormatException e) {
                        x.setValue(null);
                    }
                });
            }
        }

        Optional<ItemStackSnapshot> oiss = Sponge.getDataManager().deserialize(ItemStackSnapshot.class,
                ConfigurateTranslator.instance().translateFrom(value));
        if (oiss.isPresent()) {
            return oiss.get();
        }

        // If we get here, we have had a problem with the data. We should therefore remove all the data
        // (except enchants) and see what happens.
        ConfigurationNode unsafe = value.getNode("UnsafeData");
        unsafe.getChildrenMap().forEach((k, v) -> {
            if (!k.toString().equalsIgnoreCase("ench")) {
                v.setValue(null);
            }
        });

        // Try again
        oiss = Sponge.getDataManager().deserialize(ItemStackSnapshot.class, ConfigurateTranslator.instance().translateFrom(value));
        if (oiss.isPresent()) {
            Nucleus.getNucleus().getLogger().warn(Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("config.itemstacksnapshot.data", value.getNode("ItemType").getString()));
            return oiss.get();
        }

        Nucleus.getNucleus().getLogger().warn(Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("config.itemstacksnapshot.unable", value.getNode("ItemType").getString()));

        // Return an empty snapshot
        return ItemStackSnapshot.NONE;
    }

    @Override
    public void serialize(TypeToken<?> type, ItemStackSnapshot obj, ConfigurationNode value) throws ObjectMappingException {
        DataContainer container = obj.toContainer();
        ConfigurationNode root = ConfigurateTranslator.instance().translateData(container);
        ConfigurationNode ench = root.getNode("UnsafeData", "ench");

        // Remove empty enchantment list.
        if (!ench.isVirtual() && !ench.hasListChildren()) {
            root.getNode("UnsafeData").removeChild("ench");
        }

        value.setValue(root);
    }
}

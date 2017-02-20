/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.typeserialisers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.configurate.wrappers.NucleusItemStackSnapshot;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;
import java.util.stream.Collectors;

public class NucleusItemStackSnapshotSerialiser implements TypeSerializer<NucleusItemStackSnapshot> {

    private final TypeToken<ItemStackSnapshot> iss = TypeToken.of(ItemStackSnapshot.class);

    @Override
    public NucleusItemStackSnapshot deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        // Process enchantments, temporary fix before Sponge gets a more general fix in.
        boolean emptyEnchant = false;
        ConfigurationNode ench = value.getNode("UnsafeData", "ench");
        if (!ench.isVirtual()) {
            List<? extends ConfigurationNode> enchantments = ench.getChildrenList();
            if (enchantments.isEmpty()) {
                // Remove empty enchantment list.
                value.getNode("UnsafeData").removeChild("ench");
            } else {
                enchantments.forEach(x -> {
                    try {
                        short id = Short.parseShort(x.getNode("id").getString());
                        short lvl = Short.parseShort(x.getNode("lvl").getString());

                        x.getNode("id").setValue(id);
                        x.getNode("lvl").setValue(lvl);
                    } catch (NumberFormatException e) {
                        x.setValue(null);
                    }
                });
            }
        }

        ConfigurationNode data = value.getNode("Data");
        if (!data.isVirtual() && data.hasListChildren()) {
            List<? extends ConfigurationNode> n = data.getChildrenList().stream()
                .filter(x ->
                    !x.getNode("DataClass").getString().endsWith("SpongeEnchantmentData")
                    || (!x.getNode("ManipulatorData", "ItemEnchantments").isVirtual() && x.getNode("ManipulatorData", "ItemEnchantments").hasListChildren()))
                .collect(Collectors.toList());
            emptyEnchant = n.size() != data.getChildrenList().size();

            if (emptyEnchant) {
                if (n.isEmpty()) {
                    value.removeChild("Data");
                } else {
                    value.getNode("Data").setValue(n);
                }
            }
        }

        ItemStackSnapshot snapshot = value.getValue(iss);
        if (emptyEnchant) {
            ItemStack is = snapshot.createStack();
            is.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList());
            return new NucleusItemStackSnapshot(is.createSnapshot());
        }

        if (snapshot.get(Keys.ITEM_ENCHANTMENTS).isPresent()) {
            ItemStack is = snapshot.createStack();
            // Reset the data.
            is.offer(Keys.ITEM_ENCHANTMENTS, snapshot.get(Keys.ITEM_ENCHANTMENTS).get());
            return new NucleusItemStackSnapshot(is.createSnapshot());
        }

        return new NucleusItemStackSnapshot(snapshot);
    }

    @Override public void serialize(TypeToken<?> type, NucleusItemStackSnapshot obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(iss, obj.getSnapshot());
    }
}

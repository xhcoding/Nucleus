/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.typeserialisers;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;

public class ItemStackSnapshotSerialiser implements TypeSerializer<ItemStackSnapshot> {

    private final TypeToken<ItemStackSnapshot> tt = TypeToken.of(ItemStackSnapshot.class);

    @Override
    public ItemStackSnapshot deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        // Process enchantments, temporary fix before Sponge gets a more general fix in.
        ConfigurationNode ench = value.getNode("UnsafeData", "ench");
        if (!ench.isVirtual()) {
            List<? extends ConfigurationNode> enchantments = ench.getChildrenList();
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

        return Sponge.getDataManager().deserialize(ItemStackSnapshot.class,
                ConfigurateTranslator.instance().translateFrom(value))
                .orElseThrow(() -> new ObjectMappingException("Could not deserialize DataSerializable of type: " + tt.getRawType().getName()));
    }

    @Override
    public void serialize(TypeToken<?> type, ItemStackSnapshot obj, ConfigurationNode value) throws ObjectMappingException {
        DataContainer container = obj.toContainer();
        value.setValue(ConfigurateTranslator.instance().translateData(container));
    }
}

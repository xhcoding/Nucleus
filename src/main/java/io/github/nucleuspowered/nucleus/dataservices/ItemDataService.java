/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Map;

public class ItemDataService extends Service<Map<String, ItemDataNode>> {

    public ItemDataService(DataProvider<Map<String, ItemDataNode>> dataProvider) throws Exception {
        super(dataProvider);
    }

    public ItemDataNode getDataForItem(ItemStackSnapshot itemStackSnapshot) {
        return getDataForItem(itemStackSnapshot.getType().getId());
    }

    public ItemDataNode getDataForItem(String id) {
        Preconditions.checkNotNull(id);
        return data.getOrDefault(id.toLowerCase(), new ItemDataNode());
    }

    public void setDataForItem(ItemStackSnapshot itemStackSnapshot, ItemDataNode node) {
        setDataForItem(itemStackSnapshot.getType().getId(), node);
    }

    public void setDataForItem(String id, ItemDataNode node) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(node);
        data.put(id, node);
        save();
    }

    public void resetDataForItem(ItemStackSnapshot itemStackSnapshot) {
        resetDataForItem(itemStackSnapshot.getType().getId());
    }

    public void resetDataForItem(String id) {
        Preconditions.checkNotNull(id);
        data.remove(id);
        save();
    }
}

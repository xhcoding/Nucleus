/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.services;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.api.service.NucleusServerShopService;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class ItemWorthService implements NucleusServerShopService {

    private final NucleusPlugin plugin;

    public ItemWorthService(NucleusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public Optional<Double> getBuyPrice(ItemStackSnapshot itemStackSnapshot) {
        Preconditions.checkNotNull(itemStackSnapshot);
        double price = plugin.getItemDataService().getDataForItem(itemStackSnapshot).getServerBuyPrice();
        if (price < 0) {
            return Optional.empty();
        }

        return Optional.of(price);
    }

    @Override public Optional<Double> getSellPrice(ItemStackSnapshot itemStackSnapshot) {
        Preconditions.checkNotNull(itemStackSnapshot);
        double price = plugin.getItemDataService().getDataForItem(itemStackSnapshot).getServerSellPrice();
        if (price < 0) {
            return Optional.empty();
        }

        return Optional.of(price);
    }

    private Tuple<Optional<BlockState>, ItemType> getType(DataHolder stack, ItemType type) {
        return Tuple.of(stack.get(Keys.ITEM_BLOCKSTATE), type);
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.handler;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusBlacklistMigrationService;
import io.github.nucleuspowered.nucleus.configurate.datatypes.item.BlacklistNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.Tuple;

import java.util.Map;
import java.util.stream.Collectors;

public class BlacklistMigrationHandler implements NucleusBlacklistMigrationService {

    @Override public Map<BlockState, Result> getBlacklistedBlockstates() {
        ItemDataService service = Nucleus.getNucleus().getItemDataService();
        return service.getAllBlacklistedItemsByCatalogType().entrySet().stream()
                .filter(x -> x.getKey() instanceof BlockState)
                .map(x -> Tuple.of((BlockState) x.getKey(), new BlacklistResult(x.getValue())))
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    @Override public Map<ItemType, Result> getBlacklistedItemtypes() {
        ItemDataService service = Nucleus.getNucleus().getItemDataService();
        return service.getAllBlacklistedItemsByCatalogType().entrySet().stream()
                .filter(x -> x.getKey() instanceof ItemType)
                .map(x -> Tuple.of((ItemType) x.getKey(), new BlacklistResult(x.getValue())))
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    private static class BlacklistResult implements Result {

        private final boolean env;
        private final boolean use;
        private final boolean pos;

        private BlacklistResult(BlacklistNode node) {
            this.env = node.isEnvironment();
            this.use = node.isUse();
            this.pos = node.isInventory();
        }

        @Override public boolean environment() {
            return this.env;
        }

        @Override public boolean use() {
            return this.use;
        }

        @Override public boolean possession() {
            return this.pos;
        }
    }
}

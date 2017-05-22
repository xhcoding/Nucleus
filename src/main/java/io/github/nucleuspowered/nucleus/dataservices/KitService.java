/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.configurate.datatypes.KitConfigDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.KitDataNode;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;

import java.util.Map;
import java.util.Optional;

public class KitService extends AbstractService<KitConfigDataNode> {

    public KitService(DataProvider<KitConfigDataNode> dataProvider) throws Exception {
        super(dataProvider, false);
    }

    @Override public void loadInternal() throws Exception {
        super.loadInternal();

        // Migrate to new first join kit structure.
        if (data.migrate()) {
            save();
        }
    }

    public Optional<KitDataNode> getKit(String name) {
        return Util.getKeyIgnoreCase(data.getKits(), name).map(s -> data.getKits().get(s));
    }

    public Map<String, Kit> getKits() {
        return ImmutableMap.copyOf(data.getKits());
    }

    public boolean addKit(String name, KitDataNode kit) {
        if (data.getKits().keySet().stream().anyMatch(name::equalsIgnoreCase)) {
            return false;
        }

        data.getKits().put(name, kit);
        return true;
    }

    public boolean removeKit(String name) {
        Map<String, KitDataNode> msk = data.getKits();
        Optional<String> key = msk.keySet().stream().filter(name::equalsIgnoreCase).findFirst();
        return key.isPresent() && data.getKits().remove(key.get()) != null;
    }
}

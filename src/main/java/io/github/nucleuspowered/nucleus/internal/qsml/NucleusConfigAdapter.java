/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

public abstract class NucleusConfigAdapter<R> extends AbstractConfigAdapter<R> {

    public final R getNodeOrDefault() {
        try {
            return getNode();
        } catch (ObjectMappingException e) {
            return getDefaultObject();
        }
    }

    protected abstract R getDefaultObject();

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        R o = getDefaultObject();
        try {
            return node.setValue(TypeToken.of((Class<R>)o.getClass()), o);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return node;
        }
    }
}

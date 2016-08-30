/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.configurate.objectmapper.NucleusObjectMapperFactory;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.ItemStackSnapshotSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.SetTypeSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.Vector3dTypeSerialiser;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.slf4j.Logger;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Set;

public class ConfigurateHelper {

    private ConfigurateHelper() {}

    /**
     * Set Nucleus specific options on the {@link ConfigurationOptions}
     *
     * @param logger The Nucleus {@link Logger}
     * @param options The {@link ConfigurationOptions} to alter.
     * @return The {@link ConfigurationOptions}, for easier inline use of this function.
     */
    public static ConfigurationOptions setOptions(Logger logger, ConfigurationOptions options) {
        TypeSerializerCollection tsc = options.getSerializers();

        // Custom type serialisers for Nucleus
        tsc.registerType(TypeToken.of(Vector3d.class), new Vector3dTypeSerialiser());
        tsc.registerType(TypeToken.of(ItemStackSnapshot.class), new ItemStackSnapshotSerialiser(logger));
        tsc.registerPredicate(
                typeToken -> Set.class.isAssignableFrom(typeToken.getRawType()),
                new SetTypeSerialiser()
        );

        // Allows us to use localised comments and @ProcessSetting annotations
        return options.setSerializers(tsc).setObjectMapperFactory(NucleusObjectMapperFactory.getInstance());
    }
}

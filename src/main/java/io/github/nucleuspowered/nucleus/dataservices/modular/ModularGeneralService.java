/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import ninja.leaping.configurate.ConfigurationNode;

public class ModularGeneralService extends ModularDataService<ModularGeneralService> {

    public ModularGeneralService(DataProvider<ConfigurationNode> dataProvider) throws Exception {
        super(dataProvider, false);
    }
}

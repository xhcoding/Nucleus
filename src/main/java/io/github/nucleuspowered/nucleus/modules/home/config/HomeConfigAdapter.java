/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.config;

import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;

public class HomeConfigAdapter extends NucleusConfigAdapter.Standard<HomeConfig> {

    public HomeConfigAdapter() {
        super(HomeConfig.class);
    }

    @Override
    protected HomeConfig getDefaultObject() {
        return new HomeConfig();
    }
}

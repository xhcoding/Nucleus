/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.internal.annotations.EntryPoint;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;

import java.util.Map;

import javax.inject.Inject;

@EntryPoint
public abstract class ListenerBase {
    @Inject
    protected NucleusPlugin plugin;

    public Map<String, PermissionInformation> getPermissions() {
        return Maps.newHashMap();
    }

    public interface Conditional {

        boolean shouldEnable();
    }

}

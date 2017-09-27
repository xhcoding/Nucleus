/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.internal.annotations.EntryPoint;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionHandlerTrait;

import java.util.Map;

@EntryPoint
@Store(Constants.LISTENER)
public abstract class ListenerBase implements InternalServiceManagerTrait, PermissionHandlerTrait {

    protected final Nucleus plugin = NucleusPlugin.getNucleus();

    public Map<String, PermissionInformation> getPermissions() {
        return Maps.newHashMap();
    }

    public interface Conditional {

        boolean shouldEnable();
    }

}

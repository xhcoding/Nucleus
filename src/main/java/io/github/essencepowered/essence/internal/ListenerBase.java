/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.essencepowered.essence.Essence;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;

import java.util.Map;

public abstract class ListenerBase {
    @Inject
    protected Essence plugin;

    protected Map<String, PermissionInformation> getPermissions() {
        return Maps.newHashMap();
    }
}

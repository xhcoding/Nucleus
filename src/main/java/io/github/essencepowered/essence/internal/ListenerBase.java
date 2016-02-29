/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.essencepowered.essence.QuickStart;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;

import java.util.Map;

public abstract class ListenerBase {
    @Inject
    protected QuickStart plugin;

    protected Map<String, PermissionInformation> getPermissions() {
        return Maps.newHashMap();
    }
}

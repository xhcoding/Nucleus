/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.PermissionInformation;

import java.util.Map;

public abstract class ListenerBase {
    @Inject
    protected QuickStart plugin;

    protected Map<String, PermissionInformation> getPermissions() {
        return Maps.newHashMap();
    }
}

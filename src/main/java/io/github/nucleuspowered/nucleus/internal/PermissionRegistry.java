/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;

import java.util.HashMap;
import java.util.Map;

public class PermissionRegistry {

    public final static String PERMISSIONS_PREFIX = PluginInfo.ID + ".";
    private final Map<Class<? extends AbstractCommand>, CommandPermissionHandler> serviceRegistry = Maps.newHashMap();
    private final Map<String, PermissionInformation> otherPermissions = Maps.newHashMap();

    public CommandPermissionHandler getPermissionsForNucleusCommand(Class<? extends AbstractCommand> command) {
        return serviceRegistry.getOrDefault(command, new CommandPermissionHandler(command, Nucleus.getNucleus()));
    }

    public void addHandler(Class<? extends AbstractCommand> cb, CommandPermissionHandler cph) {
        if (serviceRegistry.containsKey(cb)) {
            // Silently discard.
            return;
        }

        serviceRegistry.put(cb, cph);
    }

    public void registerOtherPermission(String otherPermission, PermissionInformation pi) {
        if (otherPermissions.containsKey(otherPermission)) {
            // Silently discard.
            return;
        }

        otherPermissions.put(otherPermission, pi);
    }

    public void registerOtherPermission(String otherPermission, String description, SuggestedLevel level) {
        this.registerOtherPermission(otherPermission, new PermissionInformation(description, level));
    }

    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> m = new HashMap<>();
        serviceRegistry.values().forEach(x -> m.putAll(x.getSuggestedPermissions()));
        m.putAll(otherPermissions);
        return m;
    }
}

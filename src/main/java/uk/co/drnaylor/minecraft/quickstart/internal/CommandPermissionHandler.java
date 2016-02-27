/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.service.permission.Subject;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.PermissionInformation;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.SuggestedLevel;

import java.util.Map;

public class CommandPermissionHandler {

    public final static String PERMISSIONS_PREFIX = "quickstart.";
    private final Map<String, PermissionInformation> mssl = Maps.newHashMap();
    private final String prefix;
    private final String base;
    private final String warmup;
    private final String cooldown;
    private final String cost;

    public CommandPermissionHandler(CommandBase cb) {
        Permissions c = cb.getClass().getAnnotation(Permissions.class);
        Preconditions.checkNotNull(c);

        StringBuilder sb = new StringBuilder(PERMISSIONS_PREFIX);
        if (!c.root().isEmpty()) {
            sb.append(c.root()).append(".");
        }

        if (c.alias().isEmpty()) {
            sb.append(cb.getAliases()[0]);
        } else {
            sb.append(c.alias());
        }

        sb.append(".");
        if (!c.sub().isEmpty()) {
            sb.append(c.sub()).append(".");
        }

        prefix = sb.toString();

        base = prefix + "base";

        // Get command name.
        String command = cb.getAliases()[0];
        ChildOf co = cb.getClass().getAnnotation(ChildOf.class);
        if (co != null) {
            command = String.format("%s %s", co.parentCommand(), command);
        }

        mssl.put(base, new PermissionInformation(Util.getMessageWithFormat("permission.base", command), c.suggestedLevel()));

        warmup = prefix + "exempt.warmup";
        cooldown = prefix + "exempt.cooldown";
        cost = prefix + "exempt.cost";

        if (!cb.getClass().isAnnotationPresent(NoWarmup.class)) {
            mssl.put(warmup, new PermissionInformation(Util.getMessageWithFormat("permission.exempt.warmup", command), SuggestedLevel.ADMIN));
        }

        if (!cb.getClass().isAnnotationPresent(NoCooldown.class)) {
            mssl.put(cooldown, new PermissionInformation(Util.getMessageWithFormat("permission.exempt.cooldown", command), SuggestedLevel.ADMIN));
        }

        if (!cb.getClass().isAnnotationPresent(NoCost.class)) {
            mssl.put(cost, new PermissionInformation(Util.getMessageWithFormat("permission.exempt.cost", command), SuggestedLevel.ADMIN));
        }

        cb.plugin.getPermissionRegistry().addHandler(cb.getClass(), this);
    }

    public boolean testBase(Subject src) {
        return src.hasPermission(base);
    }

    public boolean testWarmupExempt(Subject src) {
        return src.hasPermission(warmup);
    }

    public boolean testCooldownExempt(Subject src) {
        return src.hasPermission(cooldown);
    }

    public boolean testCostExempt(Subject src) {
        return src.hasPermission(cost);
    }

    public void registerPermssionSuffix(String suffix, PermissionInformation pi) {
        this.mssl.put(prefix + suffix, pi);
    }

    public void registerPermssionSuffix(String suffix, SuggestedLevel level, String description) {
        registerPermssionSuffix(suffix, new PermissionInformation(description, level));
    }

    public void registerPermssion(String permission, SuggestedLevel level, String description) {
        registerPermssion(permission, new PermissionInformation(description, level));
    }

    public void registerPermssion(String permission, PermissionInformation pi) {
        this.mssl.put(permission, pi);
    }

    public boolean testSuffix(Subject src, String suffix) {
        return src.hasPermission(prefix + suffix);
    }

    public String getPermissionWithSuffix(String suffix) {
        return prefix + suffix;
    }

    public Map<String, PermissionInformation> getSuggestedPermissions() {
        return ImmutableMap.copyOf(mssl);
    }

}

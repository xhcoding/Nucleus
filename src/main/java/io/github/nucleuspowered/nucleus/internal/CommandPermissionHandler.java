/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.service.permission.Subject;

import java.lang.annotation.Annotation;
import java.util.Map;

public class CommandPermissionHandler {

    private final Map<String, PermissionInformation> mssl = Maps.newHashMap();
    private final String prefix;
    private final String base;
    private final String warmup;
    private final String cooldown;
    private final String cost;
    private final String selectors;

    private final boolean justReturnTrue;

    public CommandPermissionHandler(AbstractCommand<?> cb, Nucleus plugin) {
        justReturnTrue = cb.getClass().isAnnotationPresent(NoPermissions.class);

        // If there are no permissions to assign, we just return true.
        if (justReturnTrue) {
            prefix = "";
            base = "";
            warmup = "";
            cooldown = "";
            cost = "";
            selectors = "";
            return;
        }

        Permissions c = cb.getClass().getAnnotation(Permissions.class);
        if (c == null) {
            c = new Permissions() {
                @Override
                public String[] value() {
                    return new String[0];
                }

                @Override
                public String alias() {
                    return "";
                }

                @Override
                public String root() {
                    return "";
                }

                @Override
                public String sub() {
                    return "";
                }

                @Override
                public boolean supportsSelectors() {
                    return false;
                }

                @Override
                public SuggestedLevel suggestedLevel() {
                    return SuggestedLevel.ADMIN;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Permissions.class;
                }
            };
        }

        StringBuilder sb = new StringBuilder(PermissionRegistry.PERMISSIONS_PREFIX);
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
        selectors = prefix + "selectors";

        // Get command name.
        String command = cb.getAliases()[0];
        RegisterCommand co = cb.getClass().getAnnotation(RegisterCommand.class);
        if (co.subcommandOf() != AbstractCommand.class) {
            command = String.format("%s %s", co.subcommandOf().getAnnotation(RegisterCommand.class).value()[0], command);
        }

        mssl.put(base, new PermissionInformation(Util.getMessageWithFormat("permission.base", command), c.suggestedLevel()));

        if (c.supportsSelectors()) {
            mssl.put(selectors, new PermissionInformation(Util.getMessageWithFormat("permission.selector", command), c.suggestedLevel()));
        }

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

        plugin.getPermissionRegistry().addHandler(cb.getClass(), this);
    }

    public boolean isPassthrough() {
        return justReturnTrue;
    }

    public String getBase() {
        return base;
    }

    public boolean testBase(Subject src) {
        return test(src, base);
    }

    public boolean testWarmupExempt(Subject src) {
        return test(src, warmup);
    }

    public boolean testCooldownExempt(Subject src) {
        return test(src, cooldown);
    }

    public boolean testCostExempt(Subject src) {
        return test(src, cost);
    }

    public boolean testSelectors(Subject src) {
        return test(src, selectors);
    }

    public void registerPermssionSuffix(String suffix, PermissionInformation pi) {
        this.mssl.put(prefix + suffix, pi);
    }

    public void registerPermssion(String permission, PermissionInformation pi) {
        this.mssl.put(permission, pi);
    }

    public boolean testSuffix(Subject src, String suffix) {
        return test(src, prefix + suffix);
    }

    public String getPermissionWithSuffix(String suffix) {
        return prefix + suffix;
    }

    public Map<String, PermissionInformation> getSuggestedPermissions() {
        return ImmutableMap.copyOf(mssl);
    }

    private boolean test(Subject src, String permission) {
        return justReturnTrue || src.hasPermission(permission);
    }
}

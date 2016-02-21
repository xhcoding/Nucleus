/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.base.Preconditions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PermissionUtil {

    public final static String PERMISSIONS_PREFIX = "quickstart.";
    public final static String PERMISSIONS_ADMIN = PERMISSIONS_PREFIX + "admin";
    public final static String PERMISSIONS_MOD = PERMISSIONS_PREFIX + "mod";
    public final static String PERMISSIONS_USER = PERMISSIONS_PREFIX + "user";

    private final Permissions ps;
    private final String commandAlias;

    public PermissionUtil(Permissions ps, String commandAlias) {
        Preconditions.checkNotNull(ps);
        this.ps = ps;
        this.commandAlias = ps.alias().isEmpty() ? commandAlias : ps.alias().toLowerCase();
    }

    public Set<String> getBasePermissions() {
        Set<String> perms = getPermissionWithSuffix("base", PermissionLevel.DEFAULT_USER);
        perms.addAll(Arrays.asList(ps.value()));
        return perms;
    }

    public Set<String> getWarmupPermissions() {
        Set<String> perms = ps.useDefaultWarmupExempt() ? getPermissionWithSuffix("exempt.warmup") : new HashSet<>();
        perms.addAll(Arrays.asList(ps.warmupExempt()));
        return perms;
    }

    public Set<String> getCooldownPermissions() {
        Set<String> perms = ps.useDefaultCooldownExempt() ? getPermissionWithSuffix("exempt.cooldown") : new HashSet<>();
        perms.addAll(Arrays.asList(ps.cooldownExempt()));
        return perms;
    }

    public Set<String> getCostPermissions() {
        Set<String> perms = ps.useDefaultCostExempt() ? getPermissionWithSuffix("exempt.cost") : new HashSet<>();
        perms.addAll(Arrays.asList(ps.cooldownExempt()));
        return perms;
    }

    public Set<String> getPermissionWithSuffixFromRootOnly(String suffix) {
        return getPermissionWithSuffixFromRootOnly(suffix, PermissionLevel.DEFAULT);
    }

    public Set<String> getPermissionWithSuffixFromRootOnly(String suffix, PermissionLevel level) {
        Set<String> perms = new HashSet<>();

        if (ps.useDefault()) {
            StringBuilder perm = new StringBuilder(PERMISSIONS_PREFIX);
            if (!ps.root().isEmpty()) {
                perm.append(ps.root()).append(".");
            }

            perms.add(perm.append(suffix).toString());
        }

        return includeStock(perms, level);
    }

    public Set<String> getPermissionWithSuffix(String suffix) {
        return getPermissionWithSuffix(suffix, PermissionLevel.DEFAULT);
    }

    public Set<String> getPermissionWithSuffix(String suffix, PermissionLevel pl) {
        Set<String> perms = new HashSet<>();

        if (ps.useDefault()) {
            StringBuilder perm = new StringBuilder(PERMISSIONS_PREFIX);
            if (!ps.root().isEmpty()) {
                perm.append(ps.root()).append(".");
            }

            perm.append(commandAlias).append(".");

            if (!ps.sub().isEmpty()) {
                perm.append(ps.sub()).append(".");
            }

            perms.add(perm.append(suffix).toString());
        }

        return includeStock(perms, pl);
    }

    private Set<String> includeStock(Set<String> perms, PermissionLevel pl) {
        if ((pl == PermissionLevel.DEFAULT || pl == PermissionLevel.DEFAULT_USER) && ps.includeAdmin() || pl.key >= PermissionLevel.ADMIN.key) {
            perms.add(PERMISSIONS_ADMIN);
        }

        if (pl == PermissionLevel.DEFAULT_USER && ps.includeMod() || pl.key >= PermissionLevel.MOD.key) {
            perms.add(PERMISSIONS_MOD);
        }

        if (pl == PermissionLevel.DEFAULT_USER && ps.includeUser() || pl.key >= PermissionLevel.USER.key) {
            perms.add(PERMISSIONS_USER);
        }

        return perms;
    }

    public enum PermissionLevel {
        NONE(0),
        ADMIN(1),
        MOD(2),
        USER(3),
        DEFAULT(-1),
        DEFAULT_USER(-2);

        private final int key;

        PermissionLevel(int key) {
            this.key = key;
        }
    }
}

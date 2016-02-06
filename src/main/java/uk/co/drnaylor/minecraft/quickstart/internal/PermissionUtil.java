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

    PermissionUtil(Permissions ps, String commandAlias) {
        Preconditions.checkNotNull(ps);
        this.ps = ps;
        this.commandAlias = ps.alias().isEmpty() ? commandAlias : ps.alias().toLowerCase();
    }

    public Set<String> getBasePermissions() {
        Set<String> perms = getPermissionWithSuffix("base");
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

    public Set<String> getPermissionWithSuffix(String suffix) {
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

        if (ps.includeAdmin()) {
            perms.add(PERMISSIONS_ADMIN);
        }

        if (ps.includeMod()) {
            perms.add(PERMISSIONS_MOD);
        }

        if (ps.includeUser()) {
            perms.add(PERMISSIONS_USER);
        }

        return perms;
    }
}

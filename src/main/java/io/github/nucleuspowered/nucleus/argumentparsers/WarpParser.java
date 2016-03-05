/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.nucleuspowered.nucleus.PluginInfo.ERROR_MESSAGE_PREFIX;

/**
 * Returns a {@link WarpData}
 */
public class WarpParser extends CommandElement {

    private final boolean includeWarpData;
    private NucleusWarpService service;
    private final Nucleus plugin;
    private final boolean permissionCheck;

    public WarpParser(@Nullable Text key, Nucleus plugin, boolean permissionCheck) {
        this(key, plugin, permissionCheck, true);
    }

    public WarpParser(@Nullable Text key, Nucleus plugin, boolean permissionCheck, boolean includeWarpData) {
        super(key);
        this.plugin = plugin;
        this.permissionCheck = permissionCheck;
        this.includeWarpData = includeWarpData;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        getService();

        String warpName = args.next();
        String warp = warpName.toLowerCase();
        if (!service.warpExists(warp)) {
            throw args.createError(
                    Text.builder().append(Text.of(ERROR_MESSAGE_PREFIX)).append(Util.getTextMessageWithFormat("args.warps.noexist")).build());
        }

        if (!checkPermission(source, warpName)) {
            throw args.createError(
                    Text.builder().append(Text.of(ERROR_MESSAGE_PREFIX)).append(Util.getTextMessageWithFormat("args.warps.noperms")).build());
        }

        if (includeWarpData) {
            return new WarpData(warpName,
                    service.getWarp(warp).orElseThrow(() -> args.createError(Util.getTextMessageWithFormat("args.warps.notavailable"))));
        } else {
            return new WarpData(warpName, null);
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        getService();

        try {
            String name = args.peek().toLowerCase();
            return service.getWarpNames().stream().filter(s -> s.startsWith(name)).filter(s -> !includeWarpData || service.getWarp(s).isPresent())
                    .filter(x -> checkPermission(src, name)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList();
        }
    }

    private boolean checkPermission(CommandSource src, String name) {
        if (!permissionCheck || !plugin.getConfig(ConfigMap.MAIN_CONFIG).get().useSeparatePermissionsForWarp()) {
            return true;
        }

        // No permissions, no entry!
        return src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "warps." + name.toLowerCase());
    }

    private void getService() {
        if (service == null) {
            service = Sponge.getServiceManager().provideUnchecked(NucleusWarpService.class);
        }
    }

    public class WarpData {

        public final String warp;
        public final WarpLocation loc;

        private WarpData(String warp, WarpLocation loc) {
            this.warp = warp;
            this.loc = loc;
        }
    }
}

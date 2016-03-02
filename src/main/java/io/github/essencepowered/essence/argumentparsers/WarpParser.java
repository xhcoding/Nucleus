/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.argumentparsers;

import static io.github.essencepowered.essence.PluginInfo.ERROR_MESSAGE_PREFIX;

import com.google.common.collect.Lists;
import io.github.essencepowered.essence.Essence;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.data.WarpLocation;
import io.github.essencepowered.essence.api.service.EssenceWarpService;
import io.github.essencepowered.essence.internal.ConfigMap;
import io.github.essencepowered.essence.internal.PermissionRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Returns a {@link WarpData}
 */
public class WarpParser extends CommandElement {

    private final boolean includeWarpData;
    private EssenceWarpService service;
    private final Essence plugin;
    private final boolean permissionCheck;

    public WarpParser(@Nullable Text key, Essence plugin, boolean permissionCheck) {
        this(key, plugin, permissionCheck, true);
    }

    public WarpParser(@Nullable Text key, Essence plugin, boolean permissionCheck, boolean includeWarpData) {
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
            service = Sponge.getServiceManager().provideUnchecked(EssenceWarpService.class);
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

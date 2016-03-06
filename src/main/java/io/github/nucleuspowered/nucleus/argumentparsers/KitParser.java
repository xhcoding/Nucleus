/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import static io.github.nucleuspowered.nucleus.PluginInfo.ERROR_MESSAGE_PREFIX;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class KitParser extends CommandElement {

    private final Nucleus plugin;
    private final KitsConfig kitConfig;
    private final boolean permissionCheck;

    public KitParser(@Nullable Text key, Nucleus plugin, KitsConfig kitConfig, boolean permissionCheck) {
        super(key);
        this.plugin = plugin;
        this.kitConfig = kitConfig;
        this.permissionCheck = permissionCheck;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String kitName = args.next();

        if (!kitConfig.getKits().contains(kitName)) {
            throw args.createError(
                    Text.builder().append(Text.of(ERROR_MESSAGE_PREFIX)).append(Util.getTextMessageWithFormat("args.kit.noexist")).build());
        }

        if (!checkPermission(source, kitName)) {
            throw args.createError(
                    Text.builder().append(Text.of(ERROR_MESSAGE_PREFIX)).append(Util.getTextMessageWithFormat("args.kit.noperms")).build());
        }

        return kitName;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        try {
            String name = args.peek().toLowerCase();
            return kitConfig.getKits().stream().filter(s -> s.startsWith(name)).filter(s -> kitConfig.getKits().contains(s))
                    .filter(x -> checkPermission(src, name)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList();
        }
    }

    private boolean checkPermission(CommandSource src, String name) {
        if (!permissionCheck || !plugin.getConfig(ConfigMap.MAIN_CONFIG).get().useSeparatePermissionsForKits()) {
            return true;
        }

        // No permissions, no entry!
        return src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "kits." + name.toLowerCase());
    }
}

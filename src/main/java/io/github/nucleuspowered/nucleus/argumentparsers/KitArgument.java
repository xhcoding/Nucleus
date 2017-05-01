/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.modules.kit.KitModule;
import io.github.nucleuspowered.nucleus.modules.kit.commands.kit.KitCommand;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class KitArgument extends CommandElement {

    private final KitConfigAdapter config;
    private final KitHandler kitHandler;
    private final boolean permissionCheck;
    private final String showhiddenperm = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(KitCommand.class)
            .getPermissionWithSuffix("showhidden");

    public KitArgument(@Nullable Text key, boolean permissionCheck) {
        super(key);
        this.config = Nucleus.getNucleus().getConfigAdapter(KitModule.ID, KitConfigAdapter.class).get();
        this.kitHandler = Nucleus.getNucleus().getInternalServiceManager().getService(KitHandler.class).get();
        this.permissionCheck = permissionCheck;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String kitName = args.next();
        if (kitName.isEmpty()) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.kit.noname"));
        }

        Optional<Kit> kit = kitHandler.getKit(kitName);

        if (!kit.isPresent()) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.kit.noexist"));
        }

        if (!checkPermission(source, kitName, kit.get())) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.kit.noperms"));
        }

        return new KitInfo(kit.get(), kitName);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        try {
            final boolean showhidden = src.hasPermission(showhiddenperm);
            String name = args.peek().toLowerCase();
            return this.kitHandler.getKits().entrySet().stream()
                    .filter(s -> s.getKey().toLowerCase().startsWith(name))
                    .filter(x -> checkPermission(src, x.getKey(), x.getValue()))
                    .filter(x -> this.permissionCheck && (showhidden || !x.getValue().isHiddenFromList()))
                    .map(x -> x.getKey().toLowerCase())
                    .collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList();
        }
    }

    private boolean checkPermission(CommandSource src, String name, Kit kit) {
        if (!this.permissionCheck ||
                !this.config.getNodeOrDefault().isSeparatePermissions() || kit.ignoresPermission()) {
            return true;
        }

        // No permissions, no entry!
        return src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "kits." + name.toLowerCase());
    }

    public final class KitInfo {
        public final Kit kit;
        public final String name;

        public KitInfo(Kit kit, String name) {
            this.kit = kit;
            this.name = name;
        }
    }
}

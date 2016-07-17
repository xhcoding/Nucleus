/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.WarpArgument;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows a user to warp to the specified warp.
 *
 * Command Usage: /warp [warp] Permission: quickstart.warp.base
 *
 * If <code>warp.separate-permissions</code> = <code>true</code> in the commands
 * config, also requires <code>quickstart.warps.[warpname]</code> permission, or
 * the Nucleus admin permission.
 */
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("warp")
public class WarpCommand extends CommandBase<Player> {

    static final String warpNameArg = Util.getMessageWithFormat("args.name.warpname");

    @Inject private WarpConfigAdapter adapter;

    @Override
    protected Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put(PermissionRegistry.PERMISSIONS_PREFIX + "warps",
                new PermissionInformation(Util.getMessageWithFormat("permissions.warps"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments
                        .optionalWeak(GenericArguments.flags().flag("f", "-force").setAnchorFlags(false).buildWith(GenericArguments.none()))),
                GenericArguments.onlyOne(new WarpArgument(Text.of(warpNameArg), adapter, true))};
    }

    @Override
    public CommandResult executeCommand(Player pl, CommandContext args) throws Exception {
        // Permission checks are done by the parser.
        WarpArgument.WarpData wd = args.<WarpArgument.WarpData>getOne(warpNameArg).get();

        // We have a warp data, warp them.
        pl.sendMessage(Util.getTextMessageWithFormat("command.warps.start", wd.warp));

        // Warp them.
        if (args.getOne("f").isPresent()) { // Force the position.
            pl.setLocationAndRotation(wd.loc.getLocation(), wd.loc.getRotation());
        } else if (!pl.setLocationAndRotationSafely(wd.loc.getLocation(), wd.loc.getRotation())) {
            pl.sendMessage(Util.getTextMessageWithFormat("command.warps.nosafe"));

            // Don't add the cooldown if enabled.
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}

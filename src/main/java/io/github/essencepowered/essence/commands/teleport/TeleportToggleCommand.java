/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.interfaces.InternalQuickStartUser;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;

@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({ "tptoggle" })
@RunAsync
public class TeleportToggleCommand extends CommandBase<Player> {
    private final String key = "toggle";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt", new PermissionInformation(Util.getMessageWithFormat("permission.tptoggle.exempt"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(key))))
        ).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        final InternalQuickStartUser iqsu = plugin.getUserLoader().getUser(src);
        boolean flip = args.<Boolean>getOne(key).orElseGet(() -> !iqsu.isTeleportToggled());
        iqsu.setTeleportToggled(flip);
        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tptoggle.success", Util.getMessageWithFormat(flip ? "enabled" : "disabled"))));
        return CommandResult.success();
    }
}

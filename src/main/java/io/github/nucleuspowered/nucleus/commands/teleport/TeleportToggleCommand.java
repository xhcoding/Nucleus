/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.teleport;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"tptoggle"})
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
        return CommandSpec.builder().executor(this)
                .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(key))))).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        final InternalNucleusUser iqsu = plugin.getUserLoader().getUser(src);
        boolean flip = args.<Boolean>getOne(key).orElseGet(() -> !iqsu.isTeleportToggled());
        iqsu.setTeleportToggled(flip);
        src.sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.tptoggle.success"))
                .append(Util.getTextMessageWithFormat(flip ? "enabled" : "disabled")).build());
        return CommandResult.success();
    }
}

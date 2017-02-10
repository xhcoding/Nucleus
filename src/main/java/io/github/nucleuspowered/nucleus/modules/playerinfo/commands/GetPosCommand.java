/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RegisterCommand("getpos")
public class GetPosCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    private final String playerKey = "subject";

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = super.permissionSuffixesToRegister();
        mspi.put("others", new PermissionInformation(
                plugin.getMessageProvider().getMessageWithFormat("permission.getpos.others"),
                SuggestedLevel.MOD
        ));
        return mspi;
    }

    @Override protected CommandResult executeWithPlayer(CommandSource src, Player target, CommandContext args, boolean isSelf) throws Exception {
        Location<World> location = target.getLocation();
        if (isSelf) {
            src.sendMessage(
                plugin.getMessageProvider()
                    .getTextMessageWithFormat(
                        "command.getpos.location.self",
                        location.getExtent().getName(),
                        String.valueOf(location.getBlockPosition().getX()),
                        String.valueOf(location.getBlockPosition().getY()),
                        String.valueOf(location.getBlockPosition().getZ())
                    )
            );
        } else {
            src.sendMessage(
                plugin.getMessageProvider()
                    .getTextMessageWithFormat(
                        "command.getpos.location.other",
                        plugin.getNameUtil().getSerialisedName(target),
                        location.getExtent().getName(),
                        String.valueOf(location.getBlockPosition().getX()),
                        String.valueOf(location.getBlockPosition().getY()),
                        String.valueOf(location.getBlockPosition().getZ())
                )
            );
        }

        return CommandResult.success();
    }
}

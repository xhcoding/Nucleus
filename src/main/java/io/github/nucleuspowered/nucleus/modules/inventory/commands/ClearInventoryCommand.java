/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.inventory.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

@RegisterCommand({"clear", "clearinv", "clearinventory", "ci", "clearinvent"})
@NoCooldown
@NoWarmup
@NoCost
@Permissions(supportsOthers = true)
@EssentialsEquivalent({"clearinventory", "ci", "clean", "clearinvent"})
public class ClearInventoryCommand extends AbstractCommand<CommandSource> {

    private final String player = "subject";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(
                GenericArguments.requiringPermission(
                    SelectorWrapperArgument.nicknameSelector(Text.of(player), NicknameArgument.UnderlyingType.USER),
                    permissions.getPermissionWithSuffix("others")
                ))
        };
    }

    @Override protected CommandResult executeCommand(CommandSource source, CommandContext args) throws Exception {
        User user = this.getUserFromArgs(User.class, source, player, args);
        if (user.getPlayer().isPresent()) {
            Player target = user.getPlayer().get();
            Util.getStandardInventory(target).clear();
            source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.clearinventory.success", target.getName()));
            return CommandResult.success();
        } else {
            try {
                Util.getStandardInventory(user).clear();
                source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.clearinventory.success", user.getName()));
                return CommandResult.success();
            } catch (UnsupportedOperationException e) {
                throw ReturnMessageException.fromKey("command.clearinventory.offlinenotsupported");
            }
        }
    }
}

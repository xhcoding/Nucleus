/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RegisterCommand({"hat"})
@NoCooldown
@NoWarmup
@NoCost
@Permissions(supportsSelectors = true)
public class HatCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<Player> {

    private final String player = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.others", this.getAliases()[0]), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.requiringPermission(
                        GenericArguments.onlyOne(new SelectorWrapperArgument(
                                new NicknameArgument(Text.of(player), plugin.getUserDataManager(), NicknameArgument.UnderlyingType.PLAYER),
                                permissions, SelectorWrapperArgument.SINGLE_PLAYER_SELECTORS)
                            ),
                        permissions.getPermissionWithSuffix("others")))
        };
    }

    @Override
    public CommandResult executeCommand(Player pl, CommandContext args) throws Exception {
        Optional<Player> opl = this.getUser(Player.class, pl, player, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        ItemStack stack = pl.getItemInHand().orElseThrow(() -> new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.generalerror.handempty")));
        stack.setQuantity(1);
        opl.get().setHelmet(stack);
        String itemName = stack.get(Keys.DISPLAY_NAME).orElse(Text.of(stack.getItem().getName())).toPlain();

        if (pl.get(Keys.GAME_MODE).get() == GameModes.SURVIVAL) {
            stack = pl.getItemInHand().get();

            if (stack.getQuantity() > 1) {
                stack.setQuantity(stack.getQuantity() - 1);
                pl.setItemInHand(stack);
            } else {
                pl.setItemInHand(null);
            }
        }

        pl.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.hat.success", opl.get().getName(), itemName));
        return CommandResult.success();
    }
}

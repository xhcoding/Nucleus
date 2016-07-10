/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.DataScanner;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Permissions
@RegisterCommand({"iteminfo", "itemdb"})
public class ItemInfoCommand extends CommandBase<Player> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.flags().permissionFlag(permissions.getPermissionWithSuffix("extended"), "e", "-extended")
                .buildWith(GenericArguments.none())};
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("extended", new PermissionInformation(Util.getMessageWithFormat("permission.iteminfo.extended"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            ItemStack it = player.getItemInHand(HandTypes.MAIN_HAND).get();

            List<Text> lt = new ArrayList<>();
            lt.add(Util.getTextMessageWithFormat("command.iteminfo.id", it.getItem().getId(), it.getTranslation().get()));

            if (args.hasAny("e") || args.hasAny("extended")) {
                // For each key, see if the item supports it. If so, get and
                // print the value.
                DataScanner.getInstance().getKeysForHolder(it).entrySet().stream().filter(x -> x.getValue() != null).filter(x -> {
                    // Work around a Sponge bug.
                    try {
                        return it.supports(x.getValue());
                    } catch (Exception e) {
                        return false;
                    }
                }).forEach(x -> {
                    Key<? extends BaseValue<Object>> k = (Key<? extends BaseValue<Object>>) x.getValue();
                    if (it.get(k).isPresent()) {
                        DataScanner.getText(player, "command.iteminfo.key", x.getKey(), it.get(k).get()).ifPresent(lt::add);
                    }
                });
            }

            Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder().contents(lt).padding(Text.of(TextColors.GREEN, "-"))
                    .title(Util.getTextMessageWithFormat("command.iteminfo.list.header")).sendTo(player);
            return CommandResult.success();
        }

        player.sendMessage(Util.getTextMessageWithFormat("command.iteminfo.none"));
        return CommandResult.empty();
    }
}

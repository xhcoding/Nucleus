/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.itemalias;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.ItemAliasArgument;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@Permissions(root = "plugin.itemalias")
@RegisterCommand(value = "set", subcommandOf = ItemAliasCommand.class)
public class SetItemAliasCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    @Inject private ItemDataService itemDataService;

    private final String item = "item";
    private final String alias = "alias";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(new ItemAliasArgument(Text.of(item), itemDataService)),
            GenericArguments.string(Text.of(alias))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Do we have an item or blockstate?
        String a = args.<String>getOne(alias).get().toLowerCase();
        if (itemDataService.getIdFromAlias(a).isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.inuse", a));
            return CommandResult.empty();
        }

        if (!ItemDataNode.ALIAS_PATTERN.matcher(a).matches()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.notvalid", a));
            return CommandResult.empty();
        }

        Optional<CatalogType> catalogTypeOptional = args.getOne(item);
        CatalogType type;
        if (catalogTypeOptional.isPresent()) {
            type = catalogTypeOptional.get();
        } else {
            // If player, get the item in hand, otherwise, we can't continue.
            if (src instanceof Player) {
                Player pl = (Player)src;
                if (pl.getItemInHand().isPresent()) {
                    final ItemStack is = pl.getItemInHand().get();
                    Optional<BlockState> blockState = is.get(Keys.ITEM_BLOCKSTATE);
                    if (blockState.isPresent()) {
                        type = blockState.get();
                    } else {
                        type = is.getItem();
                    }
                } else {
                    src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.noneinhand"));
                    return CommandResult.empty();
                }
            } else {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.noidconsole"));
                return CommandResult.empty();
            }
        }

        // Set the alias.
        String id = type.getId().toLowerCase();
        ItemDataNode idn = itemDataService.getDataForItem(id);
        idn.addAlias(a);
        itemDataService.setDataForItem(id, idn);

        // Tell the user
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.success", a, id));
        return CommandResult.success();
    }
}

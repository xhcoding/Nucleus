/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.util.List;

@Permissions
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
@RegisterCommand({"firstjoinkit", "starterkit", "joinkit", "firstkit"})
public class FirstKitCommand extends CommandBase<CommandSource> {

    @Inject private GeneralService gds;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        List<ItemStackSnapshot> stacks = gds.getFirstKit();
        if (stacks == null || stacks.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.firstkit.list.none"));
            return CommandResult.success();
        }

        List<Text> itemNames = Lists.newArrayList();

        stacks.forEach(x -> itemNames.add(Text.builder(x.getType().getTranslation().get()).color(TextColors.GREEN)
                .append(Text.of(TextColors.GREEN, " (" + x.getType().getId() + "): "))
                .append(Text.of(TextColors.YELLOW, x.getCount())).build()));

        PaginationService service = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        service.builder().contents(itemNames)
                .title(Util.getTextMessageWithFormat("command.firstkit.list.title")).padding(Text.of(TextColors.GREEN, "-"))
                .sendTo(src);

        return CommandResult.success();
    }
}

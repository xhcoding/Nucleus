/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;
import java.util.Set;

/**
 * Lists all the kits.
 *
 * Command Usage: /kit list Permission: nucleus.kit.list.base
 */
@Permissions(root = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"list", "ls"}, subcommandOf = KitCommand.class)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
public class KitListCommand extends CommandBase<CommandSource> {

    @Inject private KitHandler kitConfig;
    @Inject private KitConfigAdapter kca;

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        ArrayList<Text> kitText = Lists.newArrayList();

        Set<String> kits = kitConfig.getKitNames();
        if (kits.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.kit.list.empty"));
            return CommandResult.empty();
        }

        // Only show kits that the user has permission for, if needed. This is the permission "nucleus.kits.<kit>".
        kitConfig.getKitNames().stream()
                .filter(kit -> !kca.getNodeOrDefault().isSeparatePermissions() || src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "kits." + kit.toLowerCase()))
                .forEach(kit -> {
            Text item = Text.builder(kit).onClick(TextActions.runCommand("/kit " + kit))
                    .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.kit.list.text", kit))).color(TextColors.AQUA)
                    .style(TextStyles.UNDERLINE).build();
            kitText.add(item);
        });

        PaginationList.Builder paginationBuilder = paginationService.builder().contents(kitText)
                .title(Util.getTextMessageWithFormat("command.kit.list.kits")).padding(Text.of(TextColors.GREEN, "-"));
        paginationBuilder.sendTo(src);

        return CommandResult.success();
    }
}

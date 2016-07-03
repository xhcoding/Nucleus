/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.Kit;
import io.github.nucleuspowered.nucleus.dataservices.UserService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.time.Duration;
import java.time.Instant;
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
    @Inject private UserDataManager userConfigLoader;

    private CommandPermissionHandler kitPermissionHandler = null;

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        Set<String> kits = kitConfig.getKitNames();
        if (kits.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.kit.list.empty"));
            return CommandResult.empty();
        }

        // Get permission handler for the /kit command in question.
        if (kitPermissionHandler == null) {
            kitPermissionHandler = plugin.getPermissionRegistry().getService(KitCommand.class).orElse(new CommandPermissionHandler(new KitCommand(), plugin));
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        ArrayList<Text> kitText = Lists.newArrayList();

        final UserService user = src instanceof Player ? userConfigLoader.get(((Player)src).getUniqueId()).orElse(null) : null;

        // Only show kits that the user has permission for, if needed. This is the permission "nucleus.kits.<kit>".
        kitConfig.getKitNames().stream()
            .filter(kit -> !kca.getNodeOrDefault().isSeparatePermissions() || src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "kits." + kit.toLowerCase()))
            .forEach(kit -> kitConfig.getKit(kit).ifPresent(k -> kitText.add(createKit(src, user, kit, k))));

        PaginationList.Builder paginationBuilder = paginationService.builder().contents(kitText)
                .title(Util.getTextMessageWithFormat("command.kit.list.kits")).padding(Text.of(TextColors.GREEN, "-"));
        paginationBuilder.sendTo(src);

        return CommandResult.success();
    }

    private Text createKit(CommandSource source, UserService user, String kitName, Kit kitObj) {
        Text.Builder tb = Text.builder(kitName);

        if (user != null && user.getKitLastUsedTime().containsKey(kitName.toLowerCase())) {
            Player p = (Player)source;

            // If one time used...
            if (kitObj.isOneTime() && !kitPermissionHandler.testSuffix(p, "exempt.onetime")) {
                return tb.color(TextColors.RED)
                        .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.kit.list.onetime", kitName)))
                        .style(TextStyles.STRIKETHROUGH).build();
            }

            // If an interval is used...
            Duration interval = kitObj.getInterval();
            if (!interval.isZero() && !kitPermissionHandler.testCooldownExempt(p)) {

                // Get the next time the kit can be used.
                Instant next = user.getKitLastUsedTime().get(kitName.toLowerCase()).plus(interval);
                if (next.isAfter(Instant.now())) {
                    // Get the time to next usage.
                    String time = Util.getTimeToNow(next);
                    return tb.color(TextColors.RED)
                            .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.kit.list.interval", kitName, time)))
                            .style(TextStyles.STRIKETHROUGH).build();
                }
            }
        }

        // Can use.
        return tb.color(TextColors.AQUA).onClick(TextActions.runCommand("/kit " + kitName))
                .onHover(TextActions.showText(Util.getTextMessageWithFormat("command.kit.list.text", kitName)))
                .style(TextStyles.UNDERLINE).build();
    }
}

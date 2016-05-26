/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Allows a user to redeem a kit.
 *
 * Command Usage: /kit Permission: nucleus.kit.base
 */
@Permissions(suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand("kit")
@NoCost // This is determined by the kit itself.
public class KitCommand extends CommandBase<Player> {

    private final String kit = "kit";

    @Inject private KitHandler kitConfig;
    @Inject private KitConfigAdapter kca;
    @Inject private UserConfigLoader userConfigLoader;
    @Inject private EconHelper econHelper;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new KitArgument(Text.of(kit), kca, kitConfig, true))};
    }

    @Override
    protected Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put(PermissionRegistry.PERMISSIONS_PREFIX + "kits",
                new PermissionInformation(Util.getMessageWithFormat("permission.kits"), SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put("exempt.cooldown", new PermissionInformation(Util.getMessageWithFormat("permission.kit.exempt.cooldown"), SuggestedLevel.ADMIN));
        pi.put("exempt.onetime", new PermissionInformation(Util.getMessageWithFormat("permission.kit.exempt.onetime"), SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        KitArgument.KitInfo kitInfo = args.<KitArgument.KitInfo>getOne(kit).get();
        InternalNucleusUser user = userConfigLoader.getUser(player.getUniqueId());
        Kit kit = kitInfo.kit;
        String kitName = kitInfo.name;
        Instant now = Instant.now();

        double cost = kitInfo.kit.getCost();
        if (permissions.testCostExempt(player)) {
            // If exempt - no cost.
            cost = 0;
        }

        // If we have a cost for the kit, check we have funds.
        if (cost > 0 && !econHelper.hasBalance(player, cost)) {
            player.sendMessage(Util.getTextMessageWithFormat("command.kit.notenough", kitName, econHelper.getCurrencySymbol(cost)));
            return CommandResult.empty();
        }

        // If the kit was used before...
        if (user.getKitLastUsedTime().containsKey(kitName)) {

            // if it's one time only and the user does not have an exemption...
            if (kit.isOneTime() && !player.hasPermission(permissions.getPermissionWithSuffix("exempt.onetime"))) {
                // tell the user.
                player.sendMessage(Util.getTextMessageWithFormat("command.kit.onetime", kitName));
                return CommandResult.empty();
            }

            // If we have a cooldown for the kit, and we don't have permission to
            // bypass it...
            if (!permissions.testCooldownExempt(player) && kit.getInterval().getSeconds() > 0) {

                // ...and we haven't reached the cooldown point yet...
                Instant timeForNextUse = user.getKitLastUsedTime().get(kitName).plus(kit.getInterval());
                if (timeForNextUse.isAfter(now)) {
                    Duration d = Duration.between(now, timeForNextUse);

                    // tell the user.
                    player.sendMessage(Util.getTextMessageWithFormat("command.kit.cooldown", Util.getTimeStringFromSeconds(d.getSeconds()), kitName));
                    return CommandResult.empty();
                }
            }
        }

        boolean isConsumed = false;
        for (ItemStackSnapshot stack : kit.getStacks()) {
            // Give them the kit.
            InventoryTransactionResult itr = player.getInventory().offer(stack.createStack());

            // If some items were rejected...
            if (!itr.getRejectedItems().isEmpty()) {
                // ...tell the user and break out.
                player.sendMessage(Util.getTextMessageWithFormat("command.kit.fullinventory"));
                break;
            }

            isConsumed = true;
        }

        // If something was consumed, consider a success.
        if (isConsumed) {
            // Charge, if necessary
            if (cost > 0 && econHelper.economyServiceExists()) {
                econHelper.withdrawFromPlayer(player, cost);
            }

            // Register the last used time. Do it for everyone, in case
            // permissions or cooldowns change later
            user.addKitLastUsedTime(kitName, now);
            player.sendMessage(Util.getTextMessageWithFormat("command.kit.spawned", kitName));
            return CommandResult.success();
        } else {
            // Failed.
            player.sendMessage(Util.getTextMessageWithFormat("command.kit.fail", kitName));
            return CommandResult.empty();
        }
    }
}

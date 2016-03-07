/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.kit;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.argumentparsers.KitParser;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.config.serialisers.Kit;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
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
@Modules(PluginModule.KITS)
@RegisterCommand("kit")
public class KitCommand extends CommandBase<Player> {

    private final String kit = "kit";

    @Inject private KitsConfig kitConfig;
    @Inject private UserConfigLoader userConfigLoader;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).children(this.createChildCommands())
                .arguments(GenericArguments.onlyOne(new KitParser(Text.of(kit), plugin, kitConfig, true))).build();
    }

    @Override
    protected Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put(PermissionRegistry.PERMISSIONS_PREFIX + "kits", new PermissionInformation(Util.getMessageWithFormat("permission.kits"), SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put("exempt.cooldown", new PermissionInformation(Util.getMessageWithFormat("permission.kit.exempt"), SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        String kitName = args.<String>getOne(kit).get();
        InternalNucleusUser user = userConfigLoader.getUser(player.getUniqueId());
        Kit kit = kitConfig.getKit(kitName);
        Instant now = Instant.now();

        // If we have a cooldown for the kit, and we don't have permission to bypass it...
        if (!player.hasPermission(permissions.getPermissionWithSuffix("exempt.cooldown")) && kit.getInterval().getSeconds() > 0) {

            // If the kit was used before...
            if (user.getKitLastUsedTime().containsKey(kitName)) {

                // ...and we haven't reached the cooldown point yet...
                Instant timeForNextUse = user.getKitLastUsedTime().get(kitName).plus(kit.getInterval());
                if (timeForNextUse.isAfter(now)) {
                    Duration d = Duration.between(now, timeForNextUse);

                    // tell the user.
                    player.sendMessage(Util.getTextMessageWithFormat("command.kit.cooldown", Util.getTimeStringFromSeconds(d.getSeconds())));
                    return CommandResult.empty();
                } else {
                    user.removeKitLastUsedTime(kitName);
                }
            }
        }

        boolean isConsumed = false;
        for (ItemStack stack : kit.getStacks()) {
            // Give them the kit.
            InventoryTransactionResult itr = player.getInventory().offer(stack);

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
            // Register the last used time. Do it for everyone, in case permissions or cooldowns change later
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

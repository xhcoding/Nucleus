/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.kit;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.argumentparsers.KitParser;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.Kit;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.concurrent.TimeUnit;

/**
 * Allows a user to warp to the specified warp.
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
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        String kitName = args.<String>getOne(kit).get();
        InternalNucleusUser user = userConfigLoader.getUser(player.getUniqueId());
        Kit kit = kitConfig.getKit(kitName);

        if (kit.getInterval() > 0) {
            if (user.getKitLastUsedTime().containsKey(kitName)) {
                if ((user.getKitLastUsedTime().get(kitName) + kit.getInterval()) > System.currentTimeMillis()) {
                    player.sendMessage(Util.getTextMessageWithFormat("command.kit.cooldown",
                            this.getTimeRemaining(user.getKitLastUsedTime().get(kitName), kit.getInterval())));
                    return CommandResult.empty();
                } else {
                    user.removeKitLastUsedTime(kitName);
                    user.addKitLastUsedTime(kitName, System.currentTimeMillis());
                }
            } else {
                user.addKitLastUsedTime(kitName, System.currentTimeMillis());
            }
        }

        for (ItemStack stack : kit.getStacks()) {
            player.getInventory().offer(stack);
        }

        player.sendMessage(Util.getTextMessageWithFormat("command.kit.spawned", kitName));
        return CommandResult.success();
    }

    public String getTimeRemaining(long timeUsed, long interval) {
        long timeEnding = timeUsed + interval;
        return this.getDurationBreakdown(timeEnding - System.currentTimeMillis());
    }

    public String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);

        if (days > 0) {
            sb.append(days);
            sb.append(" Days ");
        }

        if (hours > 0) {
            sb.append(hours);
            sb.append(" Hours ");
        }

        if (minutes > 0) {
            sb.append(minutes);
            sb.append(" Minutes ");
        }

        if (seconds > 0) {
            sb.append(seconds);
            sb.append(" Seconds");
        }

        return (sb.toString());
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

/**
 * Allows a user to redeem a kit.
 *
 * Command Usage: /kit Permission: plugin.kit.base
 */
@Permissions(suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand("kit")
@NoCooldown // This is determined by the kit itself.
@NoCost // This is determined by the kit itself.
@NonnullByDefault
@EssentialsEquivalent(value = "kit, kits", isExact = false, notes = "'/kit' redeems, '/kits' lists.")
public class KitCommand extends AbstractCommand<Player> {

    private final String kit = "kit";

    private final KitHandler kitConfig;
    private final KitConfigAdapter kca;
    private final EconHelper econHelper;

    @Inject
    public KitCommand(KitHandler kitConfig, KitConfigAdapter kca, EconHelper econHelper) {
        this.kitConfig = kitConfig;
        this.kca = kca;
        this.econHelper = econHelper;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new KitArgument(Text.of(kit), true))
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put(PermissionRegistry.PERMISSIONS_PREFIX + "kits",
                PermissionInformation.getWithTranslation("permission.kits", SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put("exempt.cooldown", PermissionInformation.getWithTranslation("permission.kit.exempt.cooldown", SuggestedLevel.ADMIN));
        pi.put("exempt.onetime", PermissionInformation.getWithTranslation("permission.kit.exempt.onetime", SuggestedLevel.ADMIN));
        pi.put("showhidden", PermissionInformation.getWithTranslation("permission.kit.showhidden", SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        KitArgument.KitInfo kitInfo = args.<KitArgument.KitInfo>getOne(kit).get();
        Kit kit = kitInfo.kit;
        String kitName = kitInfo.name;

        double cost = kitInfo.kit.getCost();
        if (permissions.testCostExempt(player)) {
            // If exempt - no cost.
            cost = 0;
        }

        // If we have a cost for the kit, check we have funds.
        if (cost > 0 && !econHelper.hasBalance(player, cost)) {
            player.sendMessage(plugin.getMessageProvider()
                    .getTextMessageWithFormat("command.kit.notenough", kitName, econHelper.getCurrencySymbol(cost)));
            return CommandResult.empty();
        }

        boolean success = kitConfig.redeemKit(kit, kitName, player, player, true);
        if (success) {
            // Charge, if necessary
            if (cost > 0 && econHelper.economyServiceExists()) {
                econHelper.withdrawFromPlayer(player, cost);
            }

            return CommandResult.success();
        }

        return CommandResult.empty();
    }
}

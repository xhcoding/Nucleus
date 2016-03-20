/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.KitParser;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * Sets kit items.
 *
 * Command Usage: /kit set Permission: nucleus.kit.set.base
 */
@Permissions(root = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"set", "update"}, subcommandOf = KitCommand.class)
@NoWarmup
@NoCooldown
@NoCost
public class KitSetCommand extends CommandBase<Player> {

    @Inject private KitsConfig kitConfig;
    @Inject private KitConfigAdapter kca;

    private final String kit = "kit";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).description(Text.of("Sets kit items."))
                .arguments(GenericArguments.onlyOne(new KitParser(Text.of(kit), kca, kitConfig, true))).build();
    }

    @Override
    public CommandResult executeCommand(final Player player, CommandContext args) throws Exception {
        KitParser.KitInfo kitName = args.<KitParser.KitInfo>getOne(kit).get();
        kitName.kit.updateKitInventory(player);
        kitConfig.save();
        player.sendMessage(Util.getTextMessageWithFormat("command.kit.set.success", kitName.name));
        return CommandResult.success();
    }
}

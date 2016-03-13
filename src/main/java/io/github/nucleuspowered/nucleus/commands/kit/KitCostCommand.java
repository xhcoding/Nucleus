/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.kit;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.KitParser;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Sets kit cost.
 *
 * Command Usage: /kit cost [kit] [cost]
 * Permission: nucleus.kit.cost.base
 */
@Permissions(root = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"cost", "setcost"}, subcommandOf = KitCommand.class)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
public class KitCostCommand extends CommandBase<CommandSource> {

    @Inject private KitsConfig kitConfig;
    @Inject private UserConfigLoader userConfigLoader;

    private final String costKey = "cost";
    private final String kitKey = "kit";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(new KitParser(Text.of(kitKey), plugin, kitConfig, false)),
                GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of(costKey)))
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        KitParser.KitInfo kitInfo = args.<KitParser.KitInfo>getOne(kitKey).get();
        double cost = args.<Double>getOne(costKey).get();

        if (cost < 0) {
            cost = 0;
        }

        kitInfo.kit.setCost(cost);
        kitConfig.save();
        src.sendMessage(Util.getTextMessageWithFormat("command.kit.cost.success", kitInfo.name, String.valueOf(cost)));
        return CommandResult.success();
    }
}

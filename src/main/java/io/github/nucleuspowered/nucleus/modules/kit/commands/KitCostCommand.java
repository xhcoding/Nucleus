/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.KitParser;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
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

    @Inject private KitHandler kitConfig;
    @Inject private UserConfigLoader userConfigLoader;
    @Inject private KitConfigAdapter kca;

    private final String costKey = "cost";
    private final String kitKey = "kit";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(new KitParser(Text.of(kitKey), kca, kitConfig, false)),
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
        src.sendMessage(Util.getTextMessageWithFormat("command.kit.cost.success", kitInfo.name, String.valueOf(cost)));
        return CommandResult.success();
    }
}

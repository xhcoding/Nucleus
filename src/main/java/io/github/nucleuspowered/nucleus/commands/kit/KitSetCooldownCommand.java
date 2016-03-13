/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.kit;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.KitParser;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanParser;
import io.github.nucleuspowered.nucleus.config.KitsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.time.Duration;

/**
 * Sets kit items.
 *
 * Command Usage: /kit set Permission: nucleus.kit.set.base
 */
@Permissions(root = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"setcooldown", "setinterval"}, subcommandOf = KitCommand.class)
public class KitSetCooldownCommand extends CommandBase<Player> {

    @Inject private KitsConfig kitConfig;

    private final String kit = "kit";
    private final String duration = "duration";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).description(Text.of("Sets kit cooldown."))
                .arguments(GenericArguments.seq(GenericArguments.onlyOne(new KitParser(Text.of(kit), plugin, kitConfig, true)),
                        GenericArguments.onlyOne(new TimespanParser(Text.of(duration)))))
                .build();
    }

    @Override
    public CommandResult executeCommand(final Player player, CommandContext args) throws Exception {
        KitParser.KitInfo kitInfo = args.<KitParser.KitInfo>getOne(kit).get();
        long seconds = args.<Long>getOne(duration).get();

        // This Kit is a reference back to the version in list, so we don't need to update it explicitly
        kitInfo.kit.setInterval(Duration.ofSeconds(seconds));
        kitConfig.save();
        player.sendMessage(Util.getTextMessageWithFormat("command.kit.setcooldown.success", kitInfo.name, Util.getTimeStringFromSeconds(seconds)));
        return CommandResult.success();
    }
}

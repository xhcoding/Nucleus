/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.admin.exp;

import io.github.essencepowered.essence.argumentparsers.ExperienceLevelArgument;
import io.github.essencepowered.essence.argumentparsers.PositiveIntegerArgument;
import io.github.essencepowered.essence.commands.admin.ExperienceCommand;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@NoCooldown
@NoWarmup
@NoCost
@Permissions(root = "exp")
@RegisterCommand(value = "set", subcommandOf = ExperienceCommand.class)
public class SetExperience extends CommandBase<CommandSource> {

    private final String levelKey = "level";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.player(Text.of(ExperienceCommand.playerKey)))),
                GenericArguments.firstParsing(
                    GenericArguments.onlyOne(new ExperienceLevelArgument(Text.of(levelKey))),
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(ExperienceCommand.experienceKey)))
                )
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = this.getUser(Player.class, src, ExperienceCommand.playerKey, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        Player pl = opl.get();
        Optional<Integer> l = args.<Integer>getOne(levelKey);
        DataTransactionResult dtr;
        if (l.isPresent()) {
            dtr = pl.offer(Keys.EXPERIENCE_LEVEL, l.get());
        } else {
            dtr = pl.offer(Keys.TOTAL_EXPERIENCE, args.<Integer>getOne(ExperienceCommand.experienceKey).get());
        }

        return ExperienceCommand.tellUserAboutExperience(src, pl, dtr.isSuccessful());
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.exp;

import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.modules.admin.commands.ExperienceCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@NoCooldown
@NoWarmup
@NoCost
@Permissions(root = "exp")
@RegisterCommand(value = "give", subcommandOf = ExperienceCommand.class)
public class GiveExperience extends CommandBase<CommandSource> {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.player(Text.of(ExperienceCommand.playerKey)))),
                GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(ExperienceCommand.experienceKey)))
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = this.getUser(Player.class, src, ExperienceCommand.playerKey, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        Player pl = opl.get();
        int exp = pl.get(Keys.TOTAL_EXPERIENCE).get();
        exp += args.<Integer>getOne(ExperienceCommand.experienceKey).get();

        return ExperienceCommand.tellUserAboutExperience(src, pl, pl.offer(Keys.TOTAL_EXPERIENCE, exp).isSuccessful());
    }
}

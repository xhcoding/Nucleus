/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.admin;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.NoCooldown;
import io.github.essencepowered.essence.internal.annotations.NoCost;
import io.github.essencepowered.essence.internal.annotations.NoWarmup;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@RegisterCommand({"exp", "experience", "xp"})
@Modules(PluginModule.ADMIN)
@Permissions
@NoCooldown
@NoWarmup
@NoCost
public class ExperienceCommand extends CommandBase<CommandSource> {

    public static final String playerKey = "player";
    public static final String experienceKey = "experience";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().children(this.createChildCommands())
                .arguments(GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey)))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(playerKey).get();

        ExperienceHolderData ehd = pl.get(ExperienceHolderData.class).get();
        int exp = ehd.totalExperience().get();
        int lv = ehd.level().get();

        src.sendMessage(Util.getTextMessageWithFormat("command.exp.info", pl.getName(), String.valueOf(exp), String.valueOf(lv)));
        return CommandResult.success();
    }

    public static CommandResult tellUserAboutExperience(CommandSource src, Player pl, boolean isSuccess) {
        if (!isSuccess) {
            src.sendMessage(Util.getTextMessageWithFormat("command.exp.set.error"));
            return CommandResult.empty();
        }

        int exp = pl.get(Keys.TOTAL_EXPERIENCE).get();
        int newLvl = pl.get(Keys.EXPERIENCE_LEVEL).get();

        if (!src.equals(pl)) {
            src.sendMessage(Util.getTextMessageWithFormat("command.exp.set.new.other", pl.getName(), String.valueOf(exp), String.valueOf(newLvl)));
        }

        pl.sendMessage(Util.getTextMessageWithFormat("command.exp.set.new", String.valueOf(exp), String.valueOf(newLvl)));
        return CommandResult.success();
    }

}

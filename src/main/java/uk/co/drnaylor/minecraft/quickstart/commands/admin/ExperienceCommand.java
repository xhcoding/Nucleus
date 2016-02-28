/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.admin;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

@RegisterCommand({ "exp", "experience", "xp" })
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
        return CommandSpec.builder().children(this.createChildCommands()).arguments(
                GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey)))
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(playerKey).get();

        ExperienceHolderData ehd = pl.get(ExperienceHolderData.class).get();
        int exp = ehd.totalExperience().get();
        int lv = ehd.level().get();

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.exp.info", pl.getName(), String.valueOf(exp), String.valueOf(lv))));
        return CommandResult.success();
    }

    public static CommandResult tellUserAboutExperience(CommandSource src, Player pl, boolean isSuccess) {
        if (!isSuccess) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.exp.set.error")));
            return CommandResult.empty();
        }

        int exp = pl.get(Keys.TOTAL_EXPERIENCE).get();
        int newLvl = pl.get(Keys.EXPERIENCE_LEVEL).get();

        if (!src.equals(pl)) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.exp.set.new.other", pl.getName(), String.valueOf(exp), String.valueOf(newLvl))));
        }

        pl.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.exp.set.new", String.valueOf(exp), String.valueOf(newLvl))));
        return CommandResult.success();
    }

}

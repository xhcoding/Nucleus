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
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

@RootCommand
@Modules(PluginModule.ADMIN)
@Permissions
@NoCooldown
@NoWarmup
@NoCost
public class ExperienceCommand extends CommandBase {

    private final String playerKey = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey)))
        ).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "exp", "experience", "xp" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(playerKey).get();

        ExperienceHolderData ehd = pl.get(ExperienceHolderData.class).get();
        int exp = ehd.totalExperience().get();
        int lv = ehd.level().get();

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.exp.info", pl.getName(), String.valueOf(lv), String.valueOf(exp))));
        return CommandResult.success();
    }

    @Permissions(root = "exp")
    public static class TakeExperience extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return null;
        }

        @Override
        public String[] getAliases() {
            return new String[] { "take" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            return null;
        }
    }

    @Permissions(root = "exp")
    public static class GiveExperience extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return null;
        }

        @Override
        public String[] getAliases() {
            return new String[] { "give" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            return null;
        }
    }

    @Permissions(root = "exp")
    public static class SetExperience extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return null;
        }

        @Override
        public String[] getAliases() {
            return new String[] { "set" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            return null;
        }
    }
}

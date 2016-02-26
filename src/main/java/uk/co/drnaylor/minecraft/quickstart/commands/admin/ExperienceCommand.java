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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.PositiveIntegerArgument;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.util.Optional;

@RootCommand
@Modules(PluginModule.ADMIN)
@Permissions
@NoCooldown
@NoWarmup
@NoCost
public class ExperienceCommand extends CommandBase {

    private static final String playerKey = "player";
    private static final String experienceKey = "experience";

    @Override
    @SuppressWarnings("unchecked")
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey)))
        ).children(this.createChildCommands(TakeExperience.class, GiveExperience.class, SetExperience.class)).executor(this).build();
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

    private static CommandResult tellUserAboutExperience(CommandSource src, Player pl, boolean isSuccess) {
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

    @NoCooldown
    @NoWarmup
    @NoCost
    @Permissions(root = "exp")
    public static class TakeExperience extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return CommandSpec.builder().arguments(
                    GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey))),
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(experienceKey)))
            ).executor(this).build();
        }

        @Override
        public String[] getAliases() {
            return new String[] { "take" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            Player pl = args.<Player>getOne(playerKey).get();

            int exp = pl.get(Keys.TOTAL_EXPERIENCE).get();
            exp -= args.<Integer>getOne(experienceKey).get();

            return tellUserAboutExperience(src, pl, pl.offer(Keys.TOTAL_EXPERIENCE, exp).isSuccessful());
        }
    }

    @NoCooldown
    @NoWarmup
    @NoCost
    @Permissions(root = "exp")
    public static class GiveExperience extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return CommandSpec.builder().arguments(
                    GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey))),
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(experienceKey)))
            ).executor(this).build();
        }

        @Override
        public String[] getAliases() {
            return new String[] { "give" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            Player pl = args.<Player>getOne(playerKey).get();

            int exp = pl.get(Keys.TOTAL_EXPERIENCE).get();
            exp += args.<Integer>getOne(experienceKey).get();

            return tellUserAboutExperience(src, pl, pl.offer(Keys.TOTAL_EXPERIENCE, exp).isSuccessful());
        }
    }

    @NoCooldown
    @NoWarmup
    @NoCost
    @Permissions(root = "exp")
    public static class SetExperience extends CommandBase {

        private final String levelKey = "level";

        @Override
        public CommandSpec createSpec() {
            return CommandSpec.builder().arguments(
                    GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey))),
                    GenericArguments.firstParsing(
                        GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(levelKey))),
                        GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(experienceKey)))
                    )
            ).executor(this).build();
        }

        @Override
        public String[] getAliases() {
            return new String[] { "set" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            Player pl = args.<Player>getOne(playerKey).get();
            Optional<Integer> l = args.<Integer>getOne(levelKey);
            DataTransactionResult dtr;
            if (l.isPresent()) {
                dtr = pl.offer(Keys.EXPERIENCE_LEVEL, l.get());
            } else {
                dtr = pl.offer(Keys.TOTAL_EXPERIENCE, args.<Integer>getOne(experienceKey).get());
            }

            return tellUserAboutExperience(src, pl, dtr.isSuccessful());
        }
    }
}

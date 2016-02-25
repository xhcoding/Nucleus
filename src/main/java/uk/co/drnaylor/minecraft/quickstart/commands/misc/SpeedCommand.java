/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.misc;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RootCommand
@Modules(PluginModule.MISC)
@Permissions
public class SpeedCommand extends CommandBase<Player> {
    private final String speedKey = "speed";
    private final String typeKey = "type";

    @Override
    public CommandSpec createSpec() {
        Map<String, SpeedType> keysMap = new HashMap<>();
        keysMap.put("fly", SpeedType.FLYING);
        keysMap.put("flying", SpeedType.FLYING);
        keysMap.put("f", SpeedType.FLYING);

        keysMap.put("walk", SpeedType.WALKING);
        keysMap.put("w", SpeedType.WALKING);

        return CommandSpec.builder().arguments(
                GenericArguments.optional(GenericArguments.seq(
                    GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.choices(Text.of(typeKey), keysMap, true))),
                    GenericArguments.doubleNum(Text.of(speedKey))
                ))
        ).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "speed" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Optional<Double> ospeed = args.<Double>getOne(speedKey);
        if (!ospeed.isPresent()) {
            Text t = Text.builder()
                    .append(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.speed.walk")))
                    .append(Text.of(" "))
                    .append(Text.of(TextColors.YELLOW, src.get(Keys.WALKING_SPEED).orElse(1d)))
                    .append(Text.of(TextColors.GREEN, ", " + Util.getMessageWithFormat("command.speed.flying")))
                    .append(Text.of(TextColors.YELLOW, src.get(Keys.FLYING_SPEED).orElse(1d)))
                    .append(Text.of(TextColors.GREEN, ".")).build();

            src.sendMessage(t);

            // Don't trigger cooldowns
            return CommandResult.empty();
        }

        SpeedType key = args.<SpeedType>getOne(typeKey).orElseGet(() -> src.get(Keys.IS_FLYING).orElse(false) ? SpeedType.FLYING : SpeedType.WALKING);
        double speed = ospeed.get();

        if (speed < 0) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.speed.negative")));
            return CommandResult.empty();
        }

        DataTransactionResult dtr = src.offer(key.speedKey, speed);

        if (dtr.isSuccessful()) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.speed.success", String.valueOf(speed))));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.speed.fail")));
        return CommandResult.empty();
    }

    private enum SpeedType {
        WALKING(Keys.WALKING_SPEED),
        FLYING(Keys.FLYING_SPEED);

        final Key<Value<Double>> speedKey;

        SpeedType(Key<Value<Double>> speedKey) {
            this.speedKey = speedKey;
        }
    }
}

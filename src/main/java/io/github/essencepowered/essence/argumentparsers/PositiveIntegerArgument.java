/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.argumentparsers;

import com.google.common.collect.Lists;
import io.github.essencepowered.essence.Util;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;

import javax.annotation.Nullable;

public class PositiveIntegerArgument extends CommandElement {

    public PositiveIntegerArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        try {
            return Integer.parseUnsignedInt(args.next());
        } catch (NumberFormatException e) {
            throw args.createError(Util.getTextMessageWithFormat("args.positiveint.negative"));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList();
    }
}

/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.argumentparsers;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes an argument of the form "l30" or "lv30" or "l:30" or "lv:30". Returns the integer.
 */
public class ExperienceLevelArgument extends CommandElement {
    private final Pattern argumentPattern = Pattern.compile("^(l|lv|l:|lv:)(\\d+)$", Pattern.CASE_INSENSITIVE);

    public ExperienceLevelArgument(@Nullable Text key, boolean reuturnAsExp) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        Matcher m = argumentPattern.matcher(args.next());
        if (m.find(0)) {
            return Integer.getInteger(m.group(2));
        }

        throw args.createError(Text.of(TextColors.RED, Util.getMessageWithFormat("args.explevel.error")));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return null;
    }
}

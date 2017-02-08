/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Prevents a usage string from being shown.
 */
@NonnullByDefault
public class NoDescriptionArgument extends CommandElement {

    private final CommandElement element;

    public NoDescriptionArgument(CommandElement element) {
        super(element.getKey());
        this.element = element;
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return element.complete(src, args, context);
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        element.parse(source, args, context);
    }

    @Override public Text getUsage(CommandSource src) {
        return Text.EMPTY;
    }
}

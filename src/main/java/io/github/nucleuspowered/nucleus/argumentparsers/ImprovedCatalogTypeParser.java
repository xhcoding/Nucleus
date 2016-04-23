/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;

public class ImprovedCatalogTypeParser extends CommandElement {

    private final CommandElement wrapped;

    public ImprovedCatalogTypeParser(@Nullable Text key, Class<? extends CatalogType> type) {
        super(key);
        wrapped = GenericArguments.catalogedElement(key, type);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        String arg = args.peek();
        if (!arg.contains(":")) {
            args.next();
            Object state = args.getState();

            // May need to question this.
            args.removeArgs(state, state);
            args.insertArg("minecraft:" + arg);
        }

        wrapped.parse(source, args, context);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return wrapped.complete(src, args, context);
    }
}

/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.argumentparsers;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.internal.services.JailHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JailParser extends CommandElement {
    private JailHandler handler;

    public JailParser(@Nullable Text key, JailHandler handler) {
        super(key);
        this.handler = handler;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String a = args.next().toLowerCase();
        Optional<WarpLocation> owl = handler.getJail(a);
        if (owl.isPresent()) {
            return owl.get();
        }

        throw args.createError(Text.of(TextColors.RED, Util.getMessageWithFormat("args.jail.nojail")));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        try {
            String a = args.peek().toLowerCase();
            return handler.getJails().keySet().stream().filter(x -> x.startsWith(a)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList(handler.getJails().keySet());
        }
    }
}

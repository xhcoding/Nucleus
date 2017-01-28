/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

public class UUIDArgument<T> extends CommandElement {

    @Nullable private final Function<UUID, Optional<T>> validator;

    public UUIDArgument(@Nullable Text key, @Nullable Function<UUID, Optional<T>> validator) {
        super(key);
        this.validator = validator;
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String a = args.next();
        try {
            UUID uuid = UUID.fromString(a);
            if (validator != null) {
                return validator.apply(uuid).orElseThrow(() ->
                    args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.uuid.notvalid.nomatch")));
            }

            return uuid;
        } catch (IllegalArgumentException e) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.uuid.notvalid.malformed"));
        }
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList();
    }
}

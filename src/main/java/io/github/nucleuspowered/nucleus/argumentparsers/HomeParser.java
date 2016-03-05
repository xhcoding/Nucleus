/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeParser extends CommandElement {

    private final Nucleus plugin;

    public HomeParser(@Nullable Text key, Nucleus plugin) {
        super(key);
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if (!(source instanceof User)) {
            throw args.createError(Util.getTextMessageWithFormat("command.playeronly"));
        }

        return getHome((User) source, args.next(), args);
    }

    protected WarpLocation getHome(User user, String home, CommandArgs args) throws ArgumentParseException {
        try {
            Optional<WarpLocation> owl = plugin.getUserLoader().getUser(user).getHome(home.toLowerCase());
            if (owl.isPresent()) {
                return owl.get();
            }

            throw args.createError(Util.getTextMessageWithFormat("args.home.nohome", home.toLowerCase()));
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            throw args.createError(Text.of(TextColors.RED, "An unspecified error occured"));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        if (!(src instanceof User)) {
            return null;
        }

        User u = (User) src;
        Set<String> s;
        try {
            s = plugin.getUserLoader().getUser(u).getHomes().keySet();
        } catch (IOException | ObjectMappingException e) {
            return null;
        }

        try {
            String n = args.peek();
            return s.stream().filter(x -> n.toLowerCase().startsWith(x.toLowerCase())).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList(s);
        }
    }
}

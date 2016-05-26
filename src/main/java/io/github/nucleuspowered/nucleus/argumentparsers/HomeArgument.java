/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeArgument extends CommandElement {

    private final Nucleus plugin;
    private final CoreConfigAdapter cca;

    public HomeArgument(@Nullable Text key, Nucleus plugin, CoreConfigAdapter cca) {
        super(key);
        this.plugin = plugin;
        this.cca = cca;
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
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }

            throw args.createError(Text.of(TextColors.RED, "An unspecified error occured"));
        }

        throw args.createError(Util.getTextMessageWithFormat("args.home.nohome", home.toLowerCase()));
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
        } catch (Exception e) {
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

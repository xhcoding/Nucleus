/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.argumentparsers;

import io.github.essencepowered.essence.QuickStart;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.data.WarpLocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HomeOtherParser extends HomeParser {

    public HomeOtherParser(@Nullable Text key, QuickStart plugin) {
        super(key, plugin);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String player = args.next();
        Optional<String> ohome = args.nextIfPresent();

        if (!ohome.isPresent()) {
            throw args.createError(Text.of(TextColors.RED, Util.getMessageWithFormat("args.homeother.notenough")));
        }

        Optional<User> ouser = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(player);
        if (!ouser.isPresent()) {
            throw args.createError(Text.of(TextColors.RED, Util.getMessageWithFormat("args.homeother.nouser", player)));
        }

        User user = ouser.get();
        WarpLocation location = this.getHome(user, ohome.get().toLowerCase(), args);
        return new HomeData(user, location);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }

    @Override
    public Text getUsage(CommandSource src) {
        return Text.of("<user> <home>");
    }

    public static class HomeData {
        public final User user;
        public final WarpLocation location;

        public HomeData(User user, WarpLocation location) {
            this.user = user;
            this.location = location;
        }
    }
}

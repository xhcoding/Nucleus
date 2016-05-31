/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HomeOtherArgument extends HomeArgument {

    public HomeOtherArgument(@Nullable Text key, Nucleus plugin, CoreConfigAdapter cca) {
        super(key, plugin, cca);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String player = args.next();
        Optional<String> ohome = args.nextIfPresent();

        if (!ohome.isPresent()) {
            throw args.createError(Util.getTextMessageWithFormat("args.homeother.notenough"));
        }

        Optional<User> ouser = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(player);
        if (!ouser.isPresent()) {
            throw args.createError(Util.getTextMessageWithFormat("args.homeother.nouser", player));
        }

        User user = ouser.get();
        WarpLocation location = this.getHome(user, ohome.get(), args);
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

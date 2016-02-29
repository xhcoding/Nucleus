/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.argumentparsers;

import io.github.essencepowered.essence.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parses an argument and tries to match it up against any user, online or offline.
 */
public class UserParser extends CommandElement {
    public UserParser(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if (!args.hasNext()) {
            throw args.createError(Text.of(TextColors.RED, Util.getMessageWithFormat("args.user.none")));
        }

        String user = args.next();

        // Check for exact match from the GameProfile service first.
        UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
        Optional<GameProfile> ogp = uss.getAll().stream().filter(f -> f.getName().equalsIgnoreCase(user)).findFirst();
        if (ogp.isPresent()) {
            Optional<User> retUser = uss.get(ogp.get());
            if (retUser.isPresent()) {
                return retUser.get();
            }
        }

        // No match. Check against all online players only.
        List<User> listUser = Sponge.getGame().getServer().getOnlinePlayers().stream()
                .filter(x -> x.getName().toLowerCase().startsWith(user.toLowerCase())).collect(Collectors.toList());
        if (listUser.isEmpty()) {
            throw args.createError(Text.of(TextColors.RED, Util.getMessageWithFormat("args.user.nouser", user)));
        }

        if (listUser.size() == 1) {
            return listUser.get(0);
        }

        return listUser;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String peek;
        try {
            peek = args.peek();
        } catch (ArgumentParseException e) {
            return Sponge.getGame().getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }

        return Sponge.getGame().getServer().getOnlinePlayers().stream().filter(x -> x.getName().toLowerCase().startsWith(peek.toLowerCase()))
                .map(Player::getName).collect(Collectors.toList());
    }
}

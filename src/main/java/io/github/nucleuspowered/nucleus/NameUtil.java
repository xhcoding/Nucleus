/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;
import java.util.UUID;

public class NameUtil {
    public static Text getNameFromCommandSource(CommandSource src) {
        if (!(src instanceof User)) {
            return Text.of(src.getName());
        }

        return getName((User)src);
    }

    public static Text getName(User player, InternalNucleusUser service) {
        Optional<Text> n = service.getNicknameWithPrefix();
        if (n.isPresent()) {
            return n.get();
        }

        return getName(player);
    }

    public static Text getNameWithHover(User player, UserConfigLoader loader) {
        return getName(player, loader).toBuilder().onHover(TextActions.showText(Text.of(player.getName()))).build();
    }

    public static Text getName(User player, UserConfigLoader loader) {
        try {
            InternalNucleusUser iq = loader.getUser(player);
            return getName(player, iq);
        } catch (Exception e) {
        }

        return getName(player);
    }

    public static Text getName(User player) {
        return player.get(Keys.DISPLAY_NAME).orElse(Text.of(player.getName()));
    }

    public static String getSerialisedName(User player) {
        return TextSerializers.FORMATTING_CODE.serialize(getName(player));
    }

    public static String getNameFromUUID(UUID uuid) {
        if (Util.consoleFakeUUID.equals(uuid)) {
            return Sponge.getServer().getConsole().getName();
        }

        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> user = uss.get(uuid);
        if (user.isPresent()) {
            return user.get().getName();
        }

        return Util.getMessageWithFormat("standard.unknown");
    }
}

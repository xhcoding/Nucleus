/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class NameUtil {

    private final static Map<Character, TextColor> colourMap = Maps.newHashMap();

    static {
        colourMap.put('0', TextColors.BLACK);
        colourMap.put('1', TextColors.DARK_BLUE);
        colourMap.put('2', TextColors.DARK_GREEN);
        colourMap.put('3', TextColors.DARK_AQUA);
        colourMap.put('4', TextColors.DARK_RED);
        colourMap.put('5', TextColors.DARK_PURPLE);
        colourMap.put('6', TextColors.GOLD);
        colourMap.put('7', TextColors.GRAY);
        colourMap.put('8', TextColors.DARK_GRAY);
        colourMap.put('9', TextColors.BLUE);
        colourMap.put('a', TextColors.GREEN);
        colourMap.put('b', TextColors.AQUA);
        colourMap.put('c', TextColors.RED);
        colourMap.put('d', TextColors.LIGHT_PURPLE);
        colourMap.put('e', TextColors.YELLOW);
        colourMap.put('f', TextColors.WHITE);
    }

    public static Text getNameFromCommandSource(CommandSource src) {
        if (!(src instanceof User)) {
            return Text.of(src.getName());
        }

        return getName((User)src);
    }

    public static Text getName(User player, UserConfigLoader loader) {
        try {
            InternalNucleusUser iq = loader.getUser(player);
            return getName(player, iq);
        } catch (Exception e) {
        }

        return getName(player);
    }

    public static Text getName(User player, InternalNucleusUser service) {
        Optional<Text> n = service.getNicknameWithPrefix();
        if (n.isPresent()) {
            return Text.of(getNameColour(player), n.get().toBuilder().onHover(TextActions.showText(Text.of(player.getName()))).build());
        }

        return getName(player);
    }

    public static Text getName(User player) {
        return Text.of(getNameColour(player), player.get(Keys.DISPLAY_NAME).orElse(Text.of(player.getName()))
                .toBuilder().onHover(TextActions.showText(Text.of(player.getName()))).build());
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

    private static TextColor getNameColour(User player) {
        Optional<String> os = Util.getOptionFromSubject(player, "namecolor", "namecolour");
        if (os.isPresent()) {
            String s = os.get();
            if (s.length() == 1) {
                return colourMap.getOrDefault(s.charAt(0), TextColors.NONE);
            } else {
                return Sponge.getRegistry().getType(TextColor.class, s.toUpperCase()).orElse(TextColors.NONE);
            }
        }

        return TextColors.NONE;
    }
}

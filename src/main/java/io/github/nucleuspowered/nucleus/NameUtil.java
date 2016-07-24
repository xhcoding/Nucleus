/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.dataservices.UserService;
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

    private NameUtil() {}

    private static Nucleus plugin;

    static void supplyPlugin(Nucleus plugin) {
        if (NameUtil.plugin == null) {
            NameUtil.plugin = plugin;
        }
    }

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

    /**
     * Gets the name from a command source, getting the display name if the source is a {@link User}.
     *
     * @param src The {@link CommandSource}
     * @return The {@link Text} representing the name.
     */
    public static Text getNameFromCommandSource(CommandSource src) {
        if (!(src instanceof User)) {
            return Text.of(src.getName());
        }

        return getName((User)src);
    }

    /**
     * Gets the display name from a {@link User} as Sponge sees it.
     *
     * @param player The {@link User} to get the data from.
     * @return The {@link Text}
     */
    public static Text getName(User player) {
        Preconditions.checkNotNull(player);

        TextColor tc = getNameColour(player);
        Optional<Text> dname;
        if (player.isOnline()) {
            dname = player.getPlayer().get().get(Keys.DISPLAY_NAME);
        } else {
            dname = Optional.empty();
        }

        Text.Builder tb = null;
        if (plugin != null) {
            Optional<UserService> userService = plugin.getUserDataManager().get(player);
            if (userService.isPresent()) {
                Optional<Text> n = userService.get().getNicknameWithPrefix();
                if (n.isPresent()) {
                    tb = n.get().toBuilder();
                }
            }
        } else if (dname.isPresent()) {
            tb = dname.get().toBuilder();
        }

        if (tb == null) {
            tb = Text.builder(player.getName());
        }

        tb.onHover(TextActions.showText(Util.getTextMessageWithFormat("name.hover.ign", player.getName()))).build();
        return tb.color(tc).build();
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

    public static TextColor getColourFromString(String s) {
        if (s.length() == 1) {
            return colourMap.getOrDefault(s.charAt(0), TextColors.NONE);
        } else {
            return Sponge.getRegistry().getType(TextColor.class, s.toUpperCase()).orElse(TextColors.NONE);
        }
    }

    private static TextColor getNameColour(User player) {
        Optional<String> os = Util.getOptionFromSubject(player, "namecolor", "namecolour");
        if (os.isPresent()) {
            String s = os.get();
            return getColourFromString(s);
        }

        return TextColors.NONE;
    }
}

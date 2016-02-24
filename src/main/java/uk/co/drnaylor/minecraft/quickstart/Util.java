/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.Identifiable;
import uk.co.drnaylor.minecraft.quickstart.api.data.interfaces.EndTimestamp;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.VoidFunction;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Util {

    private Util() { }

    public static final UUID consoleFakeUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final ResourceBundle messageBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

    public static MessageChannel getMessageChannel(@Nullable Player player, String... permissions) {
        Set<MessageChannel> l = Arrays.asList(permissions).stream().map(MessageChannel::permission).collect(Collectors.toSet());
        l.add(MessageChannel.TO_CONSOLE);
        MutableMessageChannel mmc = MessageChannel.combined(l).asMutable();
        if (player != null) {
            mmc.addMember(player);
        }

        return mmc;
    }

    public static UUID getUUID(CommandSource src) {
        if (src instanceof Identifiable) {
            return ((Identifiable) src).getUniqueId();
        }

        return consoleFakeUUID;
    }

    public static String getMessageWithFormat(String key, String... substitutions) {
        return MessageFormat.format(messageBundle.getString(key), (Object[])substitutions);
    }

    public static String getTimeStringFromMillseconds(long time) {
        return getTimeStringFromSeconds(time / 1000);
    }

    public static String getTimeToNow(Instant time) {
        return getTimeStringFromSeconds(Instant.now().getEpochSecond() - time.getEpochSecond());
    }

    public static String getTimeStringFromSeconds(long time) {
        long sec = time % 60;
        long min = (time / 60) % 60;
        long hour = (time / 3600) % 24;
        long day = time / 86400;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append(" ");
            if (day > 1) {
                sb.append(Util.getMessageWithFormat("standard.days"));
            } else {
                sb.append(Util.getMessageWithFormat("standard.day"));
            }
        }

        if (hour > 0) {
            appendComma(sb);
            sb.append(hour).append(" ");
            if (hour > 1) {
                sb.append(Util.getMessageWithFormat("standard.hours"));
            } else {
                sb.append(Util.getMessageWithFormat("standard.hour"));
            }
        }

        if (min > 0) {
            appendComma(sb);
            sb.append(min).append(" ");
            if (min > 1) {
                sb.append(Util.getMessageWithFormat("standard.minutes"));
            } else {
                sb.append(Util.getMessageWithFormat("standard.minute"));
            }
        }

        if (sec > 0) {
            appendComma(sb);
            sb.append(sec).append(" ");
            if (sec > 1) {
                sb.append(Util.getMessageWithFormat("standard.seconds"));
            } else {
                sb.append(Util.getMessageWithFormat("standard.second"));
            }
        }

        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return Util.getMessageWithFormat("standard.unknown");
        }
    }

    public static <T extends EndTimestamp> Optional<T> testForEndTimestamp(Optional<T> omd, VoidFunction function) {
        if (omd.isPresent()) {
            T md = omd.get();
            if (md.getEndTimestamp().isPresent() && md.getEndTimestamp().get().isBefore(Instant.now())) {
                // Mute expired.
                function.action();
                return Optional.empty();
            }
        }

        return omd;
    }

    public static String getTimeFromTicks(long ticks) {
        if (ticks < 0 || ticks > 23999) {
            // Normalise
            ticks = ticks % 24000;
        }

        int mins = (int)((ticks % 1000) / (100./6.));
        long hours = (ticks / 1000 + 6) % 24;

        if (hours < 12) {
            long ahours = hours == 0 ? 12 : hours;
            return MessageFormat.format(messageBundle.getString("time.am"), ahours, hours, mins);
        } else {
            hours -= 12;
            long ahours = hours == 0 ? 12 : hours;
            return MessageFormat.format(messageBundle.getString("time.pm"), ahours, hours, mins);
        }
    }

    public static Optional<Player> getPlayerFromOptionalOrSource(Optional<Player> pl, CommandSource src) {
        if (pl.isPresent()) {
            return pl;
        }

        if (src instanceof Player) {
            return Optional.of((Player)src);
        }

        return Optional.empty();
    }

    private static void appendComma(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
    }
}

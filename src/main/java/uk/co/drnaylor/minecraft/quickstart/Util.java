package uk.co.drnaylor.minecraft.quickstart;

import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.data.MuteData;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;

public class Util {

    private Util() { }

    public static final UUID consoleFakeUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final ResourceBundle messageBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

    public static final String getMessageWithFormat(String key, String... substitutions) {
        return MessageFormat.format(messageBundle.getString(key), (Object)substitutions);
    }

    public static String getTimeStringFromMillseconds(long time) {
        return getTimeStringFromSeconds(time / 1000);
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
                sb.append(Util.messageBundle.getString("standard.days"));
            } else {
                sb.append(Util.messageBundle.getString("standard.day"));
            }
        }

        if (hour > 0) {
            appendComma(sb);
            sb.append(hour).append(" ");
            if (hour > 1) {
                sb.append(Util.messageBundle.getString("standard.hours"));
            } else {
                sb.append(Util.messageBundle.getString("standard.hour"));
            }
        }

        if (min > 0) {
            appendComma(sb);
            sb.append(min).append(" ");
            if (min > 1) {
                sb.append(Util.messageBundle.getString("standard.minutes"));
            } else {
                sb.append(Util.messageBundle.getString("standard.minute"));
            }
        }

        if (sec > 0) {
            appendComma(sb);
            sb.append(sec).append(" ");
            if (sec > 1) {
                sb.append(Util.messageBundle.getString("standard.seconds"));
            } else {
                sb.append(Util.messageBundle.getString("standard.second"));
            }
        }

        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return Util.messageBundle.getString("standard.unknown");
        }
    }

    public static Optional<MuteData> testForMuted(QuickStartUser user) {
        Optional<MuteData> omd = user.getMuteData();
        if (omd.isPresent()) {
            MuteData md = omd.get();
            if (md.getEndTimestamp().isPresent() && md.getEndTimestamp().get().isBefore(Instant.now())) {
                // Mute expired.
                user.removeMuteData();
                return Optional.empty();
            }
        }

        return omd;
    }

    public static Optional<Long> getSecondsToTimestamp(long timestamp) {
        long currentime = new Date().getTime();
        long time = (timestamp - currentime) / 1000L;
        return time > 0 ? Optional.of(time) : Optional.empty();
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

    private static void appendComma(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
    }
}

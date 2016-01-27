package uk.co.drnaylor.minecraft.quickstart.api.data;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.Pack200;

@ConfigSerializable
public final class MuteData {

    @Setting
    private UUID muter;

    @Setting
    private Long endtimestamp;

    @Setting
    private String reason;

    @Setting
    private Long timeFromNextLogin;

    // For Configurate
    public MuteData() { }

    public MuteData(UUID muter, String reason) {
        this(muter, null, reason);
    }

    /**
     * Creates the data.
     *
     * @param muter The UUID of the muter
     * @param endtimestamp The end timestamp in milliseconds
     * @param reason The reason
     */
    public MuteData(UUID muter, Long endtimestamp, String reason) {
        this.muter = muter;
        this.endtimestamp = endtimestamp;
        this.reason = reason;
    }

    /**
     * Creates the data.
     *
     * @param muter The UUID of the muter
     * @param reason The reason
     * @param timeFromNextLogin The time, in millseconds, to mute for from next login.
     */
    public MuteData(UUID muter, String reason, Long timeFromNextLogin) {
        this.muter = muter;
        this.timeFromNextLogin = timeFromNextLogin;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    /**
     * Gets the timestamp for the end of the mute.
     *
     * @return An {@link Instant}
     */
    public Optional<Instant> getEndTimestamp() {
        if (endtimestamp == null) {
            return Optional.empty();
        }

        return Optional.of(Instant.ofEpochSecond(endtimestamp));
    }

    public UUID getMuter() {
        return muter;
    }

    public Optional<Duration> getTimeFromNextLogin() {
        if (timeFromNextLogin == null) {
            return Optional.empty();
        }

        return Optional.of(Duration.of(timeFromNextLogin, ChronoUnit.SECONDS));
    }
}

package uk.co.drnaylor.minecraft.quickstart.api.data.mute;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Optional;
import java.util.UUID;

@ConfigSerializable
public class MuteData {

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

    public Optional<Long> getEndTimestamp() {
        return Optional.ofNullable(endtimestamp);
    }

    public UUID getMuter() {
        return muter;
    }

    public Optional<Long> getTimeFromNextLogin() {
        return Optional.ofNullable(timeFromNextLogin);
    }
}

package uk.co.drnaylor.minecraft.quickstart.api.data;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.api.data.interfaces.EndTimestamp;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@ConfigSerializable
public final class JailData extends EndTimestamp {

    @Setting
    private UUID jailer;

    @Setting
    private String jailName;

    @Setting
    private String reason;

    // Configurate
    public JailData() { }

    public JailData(UUID jailer, String jailName, String reason) {
        this.jailer = jailer;
        this.reason = reason;
        this.jailName = jailName;
    }

    public JailData(UUID jailer, String jailName, String reason, Instant endTimestamp) {
        this(jailer, jailName, reason);
        this.endtimestamp = endTimestamp.getEpochSecond();
    }

    public JailData(UUID jailer, String jailName, String reason, Duration timeFromNextLogin) {
        this(jailer, jailName, reason);
        this.timeFromNextLogin = timeFromNextLogin.getSeconds();
    }

    public String getReason() {
        return reason;
    }

    public String getJailName() {
        return jailName;
    }

    public UUID getJailer() {
        return jailer;
    }

}

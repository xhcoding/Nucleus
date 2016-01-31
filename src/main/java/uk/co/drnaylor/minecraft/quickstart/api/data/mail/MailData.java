package uk.co.drnaylor.minecraft.quickstart.api.data.mail;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.time.Instant;
import java.util.UUID;

@ConfigSerializable
public class MailData {

    @Setting
    private UUID uuid;

    @Setting
    private Instant date;

    @Setting
    private String message;

    public MailData() { }

    public MailData(UUID uuid, Instant date, String message) {
        this.uuid = uuid;
        this.date = date;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Instant getDate() {
        return date;
    }

    public UUID getUuid() {
        return uuid;
    }
}

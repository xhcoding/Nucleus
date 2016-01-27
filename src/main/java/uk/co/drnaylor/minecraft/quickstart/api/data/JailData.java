package uk.co.drnaylor.minecraft.quickstart.api.data;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.UUID;

@ConfigSerializable
public final class JailData {

    @Setting
    private UUID jailer;

    @Setting
    private String jailName;

    @Setting
    private Long endtimestamp;

    @Setting
    private String reason;

    @Setting
    private Long timeFromNextLogin;
}

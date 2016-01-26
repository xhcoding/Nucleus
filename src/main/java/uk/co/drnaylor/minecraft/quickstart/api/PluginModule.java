package uk.co.drnaylor.minecraft.quickstart.api;

public enum PluginModule {
    MESSAGES("messages"),
    WARPS("warps"),
    HOMES("homes"),
    BANS("bans"),
    JAILS("jails"),
    MUTES("mutes"),
    KICKS("kicks"),
    MAILS("mails"),
    ENVIRONMENT("environment"),
    ITEMS("items"),
    PLAYERINFO("playerinfo"),
    TELEPORT("teleport"),
    CHAT("chat"),
    MISC("misc"),
    FUN("fun"),
    AFK("afk");

    String key;

    PluginModule(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

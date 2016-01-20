package uk.co.drnaylor.minecraft.quickstart.api;

public enum PluginModule {
    MESSAGES("messages"),
    WARPS("warp"),
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
    MISC("misc");

    String key;

    PluginModule(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

package uk.co.drnaylor.minecraft.quickstart.api;

public enum PluginModule {
    MESSAGES("messages"),
    WARPS("warps"),
    HOMES("homes"),
    BANS("bans"),
    JAILS("jails"),
    MUTES("mutes");

    String key;

    PluginModule(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

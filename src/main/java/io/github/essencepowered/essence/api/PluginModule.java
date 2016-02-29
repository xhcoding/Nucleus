/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.api;

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
    AFK("afk"),
    COMMAND_LOGGING("command-logging"),
    SPAWN("spawn"),
    ADMIN("admin"),
    VANISH("vanish"),
    NICKNAME("nickname");

    String key;

    PluginModule(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

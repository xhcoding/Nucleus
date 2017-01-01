/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MessageConfig {

    @Setting(value = "helpop-prefix", comment = "loc:config.message.helpop.prefix")
    private String helpOpPrefix = "&7HelpOp: {{name}} &7> &r";

    @Setting(value = "msg-receiver-prefix", comment = "loc:config.message.receiver.prefix")
    private String messageReceiverPrefix = "&7[{{fromDisplay}}&7 -> me]: &r";

    @Setting(value = "msg-sender-prefix", comment = "loc:config.message.sender.prefix")
    private String messageSenderPrefix = "&7[me -> {{toDisplay}}&7]: &r";

    @Setting(value = "socialspy")
    private SocialSpy socialSpy = new SocialSpy();

    public String getHelpOpPrefix() {
        return helpOpPrefix;
    }

    public String getMessageReceiverPrefix() {
        return messageReceiverPrefix;
    }

    public String getMessageSenderPrefix() {
        return messageSenderPrefix;
    }

    public String getMessageSocialSpyPrefix() {
        return socialSpy.messageSocialSpyPrefix;
    }

    public boolean isSocialSpyLevels() {
        return socialSpy.socialSpyLevels;
    }

    public boolean isSocialSpySameLevel() {
        return socialSpy.socialSpySameLevel;
    }

    public int getServerLevel() {
        return socialSpy.serverLevel;
    }

    public boolean isShowMessagesInSocialSpyWhileMuted() {
        return socialSpy.showMessagesInSocialSpyWhileMuted;
    }

    public String getMutedTag() {
        return socialSpy.mutedTag;
    }

    public boolean isOnlyPlayerSocialSpy() {
        return socialSpy.onlyPlayerSocialSpy;
    }

    @ConfigSerializable
    public static class SocialSpy {
        @Setting(value = "msg-prefix", comment = "loc:config.message.socialspy.prefix")
        private String messageSocialSpyPrefix = "&7[SocialSpy] [{{fromDisplay}}&7 -> {{toDisplay}}&7]: &r";

        @Setting(value = "use-levels", comment = "loc:config.message.socialspy.levels")
        private boolean socialSpyLevels = false;

        @Setting(value = "same-levels-can-see-each-other", comment = "loc:config.message.socialspy.samelevel")
        private boolean socialSpySameLevel = true;

        @Setting(value = "server-level", comment = "loc:config.message.socialspy.serverlevel")
        private int serverLevel = Integer.MAX_VALUE;

        @Setting(value = "show-cancelled-messages", comment = "loc:config.message.socialspy.mutedshow")
        private boolean showMessagesInSocialSpyWhileMuted = false;

        @Setting(value = "cancelled-messages-tag", comment = "loc:config.message.socialspy.mutedtag")
        private String mutedTag = "&c[cancelled] ";

        @Setting(value = "show-only-players", comment = "loc:config.message.socialspy.playeronly")
        private boolean onlyPlayerSocialSpy = false;
    }
}

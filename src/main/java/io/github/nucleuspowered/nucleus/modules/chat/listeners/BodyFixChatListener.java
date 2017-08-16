/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.chat.ChatModule;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BodyFixChatListener extends ListenerBase implements ListenerBase.Conditional {

    private static final Pattern bodyPattern = Pattern.compile("^\\s*<[a-zA-Z0-9_]+>\\s*");
    private static final Pattern colorCodeAdjustment = Pattern.compile("^((&[0-9a-fklmno])+)\\s+");

    @Listener(order = Order.LATE)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        if (bodyPattern.matcher(event.getFormatter().getBody().toText().toPlain()).find()) {
            String m = TextSerializers.FORMATTING_CODE.serialize(event.getFormatter().getBody().toText());
            m = m.replaceFirst("<" + player.getName() + ">", "").trim();

            Matcher matcher = colorCodeAdjustment.matcher(m);
            if (matcher.find()) {
                m = m.replaceFirst(matcher.group(), matcher.group(1));
            }

            event.getFormatter().setBody(TextSerializers.FORMATTING_CODE.deserialize(m));
        }
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(ChatModule.ID, ChatConfigAdapter.class, ChatConfig::isCheckBody).orElse(false);
    }

}

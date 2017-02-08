/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

public class MessageChannelWrapper implements MessageChannel {

    private final MessageChannel toWrap;
    private final Collection<MessageReceiver> receivers;

    public MessageChannelWrapper(MessageChannel toWrap, Collection<MessageReceiver> receivers) {
        this.toWrap = toWrap;
        this.receivers = receivers;
    }

    public MessageChannel getToWrap() {
        return toWrap;
    }

    @Override public Collection<MessageReceiver> getMembers() {
        return receivers;
    }

    @Override public void send(@Nullable Object sender, Text original, ChatType type) {
        toWrap.send(sender, original, type);
    }

    @Override public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
        return toWrap.transformMessage(sender, recipient, original, type);
    }
}

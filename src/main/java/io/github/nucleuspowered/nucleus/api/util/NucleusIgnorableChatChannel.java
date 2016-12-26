/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.util;

import org.spongepowered.api.text.channel.MessageChannel;

/**
 * This marker interface tells Nucleus to not perform any chat formatting during the
 * {@link org.spongepowered.api.event.message.MessageChannelEvent.Chat} event.
 */
public interface NucleusIgnorableChatChannel extends MessageChannel {
}

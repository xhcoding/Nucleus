/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.chat;

import org.spongepowered.api.text.channel.MessageChannel;

/**
 * This interface is a marker to indicate to the Chat module that messages through this channel should not be formatted.
 */
public interface NucleusNoFormatChannel extends MessageChannel {}

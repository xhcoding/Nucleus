/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.exceptions;

import org.spongepowered.api.text.Text;

public class NicknameException extends Exception {

    private final Type type;
    private final Text textMessage;

    public NicknameException(Text message, Type type) {
        super(message.toPlain());
        this.textMessage = message;
        this.type = type;
    }

    public Text getTextMessage() {
        return this.textMessage;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        /**
         * If a nickname is an IGN, but not their own
         */
        NOT_OWN_IGN,

        INVALID_STYLE_OR_COLOUR,

        INVALID_PATTERN,

        TOO_SHORT,

        TOO_LONG,

        EVENT_CANCELLED
    }
}

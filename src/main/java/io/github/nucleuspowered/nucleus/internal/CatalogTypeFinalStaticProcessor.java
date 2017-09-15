/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.api.EventContexts;
import org.spongepowered.api.event.cause.EventContextKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CatalogTypeFinalStaticProcessor {

    private CatalogTypeFinalStaticProcessor() {}

    private static void setFinalStatic(Field field, Object entry) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, entry);
    }

    public static void setEventContexts() throws Exception {
        EventContextKey<Boolean> shouldFormatChannel = EventContextKey.builder(Boolean.class)
                .id(EventContexts.Identifiers.SHOULD_FORMAT_CHANNEL)
                .name("Nucleus - Context to indicate whether a chat message should be formatted.")
                .build();
        setFinalStatic(EventContexts.class.getDeclaredField("SHOULD_FORMAT_CHANNEL"), shouldFormatChannel);
    }

}

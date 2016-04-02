/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.messages.ResourceMessageProvider;
import org.junit.BeforeClass;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.FormattingCodeTextSerializer;
import org.spongepowered.api.text.serializer.SafeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class TestBase {

    private static void setFinalStatic(Field field) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private static void setFinalStaticPlain(Field field) throws Exception {
        setFinalStatic(field);

        field.set(null, new SafeTextSerializer() {
            @Override
            public Text deserialize(String input) {
                return Text.of("key");
            }

            @Override
            public String serialize(Text text) {
                return "key";
            }
        });
    }

    private static void setFinalStaticFormatters(Field field) throws Exception {
        setFinalStatic(field);

        field.set(null, new FormattingCodeTextSerializer() {
            @Override
            public char getCharacter() {
                return '&';
            }

            @Override
            public String stripCodes(String text) {
                return "test";
            }

            @Override
            public String replaceCodes(String text, char to) {
                return "test";
            }

            @Override
            public Text deserialize(String input) {
                return Text.of("key");
            }

            @Override
            public String serialize(Text text) {
                return "key";
            }
        });
    }

    @BeforeClass
    public static void testSetup() throws Exception {
        try {
            Util.setMessageProvider(ResourceMessageProvider::new);
        } catch (IllegalStateException e) {
            // Nope
        }

        setFinalStaticPlain(TextSerializers.class.getField("PLAIN"));
        setFinalStaticFormatters(TextSerializers.class.getField("FORMATTING_CODE"));
        setFinalStaticFormatters(TextSerializers.class.getField("LEGACY_FORMATTING_CODE"));
    }
}

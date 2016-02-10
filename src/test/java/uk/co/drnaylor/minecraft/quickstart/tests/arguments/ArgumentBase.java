package uk.co.drnaylor.minecraft.quickstart.tests.arguments;

import org.junit.Before;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.SafeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class ArgumentBase {

    // Thanks to http://stackoverflow.com/a/3301720
    private static void setFinalStatic(Field field) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

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

    @Before
    public void testSetup() throws Exception {
        setFinalStatic(TextSerializers.class.getField("PLAIN"));
    }
}

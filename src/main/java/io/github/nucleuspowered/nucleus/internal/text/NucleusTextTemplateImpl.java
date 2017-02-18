/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

@NonnullByDefault
public abstract class NucleusTextTemplateImpl implements NucleusTextTemplate {

    private static final Map<String, Object> emptyVariables = Maps.newHashMap();

    private final String representation;
    private final TextTemplate textTemplate;
    private static final Map<String, Function<CommandSource, Text>> tokenMap = Maps.newHashMap();

    public NucleusTextTemplateImpl(String representation) {
        this.representation = representation;
        Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> t = parse(representation);
        this.textTemplate = t.getFirst();

        tokenMap.putAll(t.getSecond());
    }

    @Override public boolean isEmpty() {
        return false;
    }

    public String getRepresentation() {
        return representation;
    }

    @Override public TextTemplate getTextTemplate() {
        return textTemplate;
    }

    abstract Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String parser);

    @Override public boolean containsTokens() {
        return !tokenMap.isEmpty();
    }

    @Override @SuppressWarnings("SameParameterValue")
    public Text getForCommandSource(CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokensArray,
            @Nullable Map<String, Object> variables) {
        final Map<String, Object> variables2 = variables == null ? emptyVariables : variables;

        Map<String, TextTemplate.Arg> tokens = textTemplate.getArguments();
        Map<String, Text> finalArgs = Maps.newHashMap();

        tokens.forEach((k, v) -> {
            String key = k.toLowerCase();
            boolean hasSpace = false;
            if (k.endsWith(":s")) {
                key = k.substring(0, k.length() - 2);
                hasSpace = true;
            }

            Text t;
            if (tokenMap.containsKey(key)) {
                t = tokenMap.get(key).apply(source);
            } else if (tokensArray != null && tokensArray.containsKey(key)) {
                t = tokensArray.get(key).apply(source).orElse(null);
            } else {
                t = Nucleus.getNucleus().getMessageTokenService().parseToken(key, source, variables2).orElse(null);
            }

            if (t != null) {
                if (hasSpace) {
                    finalArgs.put(k, Text.join(t, Util.NOT_EMPTY));
                } else {
                    finalArgs.put(k, t);
                }
            }
        });

        return textTemplate.apply(finalArgs).build();
    }

    public Text toText() {
        return textTemplate.toText();
    }

    /**
     * Creates a {@link TextTemplate} from an Ampersand encoded string.
     */
    static class Ampersand extends NucleusTextTemplateImpl {

        private static final Pattern pattern = Pattern.compile("(?!\\[[.]+\\]\\(/[^\\)]*?)\\{\\{(?!subject)([^\\s\\{\\}]+)}}(?![^\\(]*?\\))");

        Ampersand(String representation) {
            super(representation);
        }

        @Override Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String input) {
            // regex!
            final String string = NucleusTextTemplateFactory.INSTANCE.performReplacements(input);
            Matcher mat = pattern.matcher(string);
            List<String> map = Lists.newArrayList();

            while (mat.find()) {
                map.add(mat.group(1));
            }

            String[] s = pattern.split(string);

            // Generic hell.
            ArrayDeque<TextRepresentable> texts = new ArrayDeque<>();
            Map<String, Function<CommandSource, Text>> tokens = Maps.newHashMap();

            // TextParsingUtils URL parsing needed here.
            TextParsingUtils cu = Nucleus.getNucleus().getTextParsingUtils();

            // This condition only occurs if you _just_ use the token. Otherwise, you get a part either side - so it's either 0 or 2.
            if (s.length > 0) {
                cu.createTextTemplateFragmentWithLinks(s[0]).mapIfPresent(texts::addAll, tokens::putAll);
            }

            for (int i = 0; i < map.size(); i++) {
                TextTemplate.Arg.Builder arg = TextTemplate.arg(map.get(i)).optional();
                TextRepresentable r = texts.peekLast();
                if (r != null) {
                    TextParsingUtils.getLastColourAndStyle(r, null).applyTo(st -> arg.color(st.colour).style(st.style));
                }

                texts.add(arg.build());
                if (s.length > i + 1) {
                    cu.createTextTemplateFragmentWithLinks(s[i + 1]).mapIfPresent(texts::addAll, tokens::putAll);
                }
            }

            return Tuple.of(TextTemplate.of(texts.toArray(new Object[texts.size()])), tokens);
        }
    }

    static class Json extends NucleusTextTemplateImpl {

        @Nullable private static TypeSerializer<TextTemplate> textTemplateTypeSerializer = null;

        Json(String representation) throws ObjectMappingException {
            super(representation);
        }

        @Override Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String parser) {
            if (textTemplateTypeSerializer == null) {
                textTemplateTypeSerializer = ConfigurationOptions.defaults().getSerializers().get(TypeToken.of(TextTemplate.class));
            }

            try {
                return Tuple.of(textTemplateTypeSerializer.deserialize(TypeToken.of(TextTemplate.class), SimpleConfigurationNode.root().setValue(parser)),
                        Maps.newHashMap());
            } catch (ObjectMappingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Empty extends NucleusTextTemplateImpl {

        public static final NucleusTextTemplateImpl INSTANCE = new Empty();

        private Empty() {
            super("");
        }

        @Override Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String parser) {
            return Tuple.of(TextTemplate.EMPTY, Maps.newHashMap());
        }

        @Override public boolean isEmpty() {
            return true;
        }
    }
}

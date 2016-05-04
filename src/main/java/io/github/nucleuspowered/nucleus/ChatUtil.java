/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ChatUtil {

    private final Map<String, Function<Player, Text>> tokens;
    private final Pattern match;
    private final Pattern p;
    private final UserConfigLoader loader;

    ChatUtil(UserConfigLoader loader) {
        tokens = createTokens();
        StringBuilder sb = new StringBuilder("(");
        tokens.forEach((k, v) -> sb.append(k.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}")).append("|"));

        String m = sb.deleteCharAt(sb.length() - 1).append(")").toString();
        match = Pattern.compile(m, Pattern.CASE_INSENSITIVE);
        p = Pattern.compile(MessageFormat.format("(?<={0})|(?={0})", m), Pattern.CASE_INSENSITIVE);
        this.loader = loader;
    }

    // String -> Text parser. Should split on all {{}} tags, but keep the tags in. We can then use the target map
    // to do the replacements!
    public Text getFromTemplate(String template, Player player, boolean trimTrailingSpace) {

        Text.Builder tb = Text.builder();
        for (String textElement : p.split(template)) {
            if (match.matcher(textElement).matches()) {
                // If we have a token, do the replacement as specified by the function
                Text message = tokens.get(textElement.toLowerCase()).apply(player);
                if (!message.isEmpty()) {
                    trimTrailingSpace = false;
                    tb.append(message);
                }
            } else {
                if (trimTrailingSpace) {
                    textElement = textElement.replaceAll("^\\s+", "");
                }

                if (!textElement.isEmpty()) {
                    // Just convert the colour codes, but that's it.
                    tb.append(TextSerializers.formattingCode('&').deserialize(textElement));
                    trimTrailingSpace = false;
                }
            }
        }

        return tb.build();
    }

    private Map<String, Function<Player, Text>> createTokens() {
        Map<String, Function<Player, Text>> t = new HashMap<>();

        t.put("{{name}}", (p) -> Text.of(p.getName()));
        t.put("{{prefix}}", (p) -> getTextFromOption(p, "prefix"));
        t.put("{{suffix}}", (p) -> getTextFromOption(p, "suffix"));
        t.put("{{displayname}}", (p) -> NameUtil.getNameWithHover(p, loader));
        return t;
    }

    private Text getTextFromOption(Player player, String option) {
        Optional<OptionSubject> oos = Util.getSubject(player);
        if (!oos.isPresent()) {
            return Text.EMPTY;
        }

        return TextSerializers.formattingCode('&').deserialize(oos.get().getOption(option).orElse(""));
    }
}

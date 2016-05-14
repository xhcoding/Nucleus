/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private final Map<String, Function<CommandSource, Text>> tokens;
    private final Pattern match;
    private final Pattern p;
    private final UserConfigLoader loader;
    private final Pattern urlParser =
            Pattern.compile("(^|(?!\\s))(?<colour>(&[0-9a-flmnork])+)?(?<url>(http(s)?://)?([A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,}\\S*)", Pattern.CASE_INSENSITIVE);

    public ChatUtil(UserConfigLoader loader) {
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
    public Text getFromTemplate(String template, CommandSource cs, boolean trimTrailingSpace) {

        Text.Builder tb = Text.builder();
        for (String textElement : p.split(template)) {
            if (match.matcher(textElement).matches()) {
                // If we have a token, do the replacement as specified by the function
                Text message = tokens.get(textElement.toLowerCase()).apply(cs);
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

    public Text addUrlsToAmpersandFormattedString(String message) {
        Preconditions.checkNotNull(message, "message");
        if (message.isEmpty()) {
            return Text.EMPTY;
        }

        Matcher m = urlParser.matcher(message);
        if (!m.find()) {
            return TextSerializers.FORMATTING_CODE.deserialize(message);
        }

        List<Text> texts = Lists.newArrayList();
        String remaining = message;
        TextColor lastColour = TextColors.WHITE;
        TextStyle lastStyles = TextStyles.NONE;
        do {

            // We found a URL. We split on the URL that we have.
            String[] textArray = remaining.split(urlParser.pattern(), 2);
            Text first = Text.builder().color(lastColour).style(lastStyles)
                    .append(TextSerializers.FORMATTING_CODE.deserialize(textArray[0])).build();

            // Add this text to the list regardless.
            texts.add(first);

            // If we have more to do, shove it into the "remaining" variable.
            if (textArray.length == 2) {
                remaining = textArray[1];
            } else {
                remaining = null;
            }

            // Get the last colour & styles
            String colourMatch = m.group("colour");
            if (colourMatch != null && !colourMatch.isEmpty()) {
                first = TextSerializers.FORMATTING_CODE.deserialize(m.group("colour") + " ");
                while (!first.getChildren().isEmpty()) {
                    first = first.getChildren().get(0);
                }

                lastColour = first.getColor();
                lastStyles = first.getStyle();
            } else {
                while (!first.getChildren().isEmpty()) {
                    first = first.getChildren().reverse().get(0);
                }

                lastColour = first.getColor();
                lastStyles = first.getStyle();
            }

            // Build the URL
            String url = m.group("url");
            try {
                URL urlObj;
                if (!url.startsWith("http://") || !url.startsWith("https://")) {
                    urlObj = new URL("http://" + url);
                } else {
                    urlObj = new URL(url);
                }

                texts.add(Text.builder(url).color(lastColour).style(lastStyles)
                        .onHover(TextActions.showText(Util.getTextMessageWithFormat("chat.url", url)))
                        .onClick(TextActions.openUrl(urlObj))
                        .build());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } while (remaining != null && m.find());

        // Add the last bit.
        if (remaining != null) {
            texts.add(Text.builder().color(lastColour).style(lastStyles)
                    .append(TextSerializers.FORMATTING_CODE.deserialize(remaining)).build());
        }

        // Join it all together.
        return Text.join(texts);
    }

    private Map<String, Function<CommandSource, Text>> createTokens() {
        Map<String, Function<CommandSource, Text>> t = new HashMap<>();

        t.put("{{name}}", (p) -> Text.of(p.getName()));
        t.put("{{prefix}}", (p) -> getTextFromOption(p, "prefix"));
        t.put("{{suffix}}", (p) -> getTextFromOption(p, "suffix"));
        t.put("{{displayname}}", this::getName);
        return t;
    }

    private Text getTextFromOption(CommandSource cs, String option) {
        if (cs instanceof Player) {
            Optional<OptionSubject> oos = Util.getSubject((Player)cs);
            if (oos.isPresent()) {
                return TextSerializers.formattingCode('&').deserialize(oos.get().getOption(option).orElse(""));
            }
        }

        return Text.EMPTY;
    }

    private Text getName(CommandSource cs) {
        if (cs instanceof Player) {
            return NameUtil.getNameWithHover((Player)cs, loader);
        }

        return Text.of(cs.getName());
    }
}

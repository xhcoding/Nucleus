/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.LocatedSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtil {

    private final Map<String, Function<CommandSource, Text>> tokens;
    private final Map<String, Function<CommandSource, Text>> serverTokens;

    private final Pattern playerTokenMatcher;
    private final Pattern playerTokenSplitter;

    private final Pattern serverTokenMatcher;
    private final Pattern serverTokenSplitter;

    private final Nucleus plugin;
    private final Pattern urlParser =
            Pattern.compile("(?<first>(^|\\s))(?<colour>(&[0-9a-flmnork])+)?(?<url>(http(s)?://)?([A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,}\\S*)", Pattern.CASE_INSENSITIVE);

    private CoreConfigAdapter cca = null;

    public ChatUtil(Nucleus plugin) {
        tokens = createTokens();
        serverTokens = createServerTokens();

        String m = getRegex(tokens.keySet());
        playerTokenMatcher = Pattern.compile(m, Pattern.CASE_INSENSITIVE);
        playerTokenSplitter = Pattern.compile(MessageFormat.format("(?<={0})|(?={0})", m), Pattern.CASE_INSENSITIVE);

        m = getRegex(serverTokens.keySet());
        serverTokenMatcher = Pattern.compile(m, Pattern.CASE_INSENSITIVE);
        serverTokenSplitter = Pattern.compile(MessageFormat.format("(?<={0})|(?={0})", m), Pattern.CASE_INSENSITIVE);

        this.plugin = plugin;
    }

    private String getRegex(Set<String> keys) {
        StringBuilder sb = new StringBuilder("(");
        keys.forEach(k -> sb.append(k.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}")).append("|"));
        return sb.deleteCharAt(sb.length() - 1).append(")").toString();
    }

    public List<Text> getFromStrings(List<String> strings, CommandSource cs) {
        return strings.stream().map(x -> this.getServerMessageFromTemplate(x, cs, true))
                .map(x -> addUrlsToAmpersandFormattedString(TextSerializers.FORMATTING_CODE.serialize(x)))
                .collect(Collectors.toList());
    }

    public Text getMessageFromTokens(String template, CommandSource cs, boolean trimTrailingSpace,
                                     boolean includePlayer, boolean includeServer, Map<String, Function<CommandSource, Text>>... customTokens) {

        Map<String, Function<CommandSource, Text>> map = Maps.newHashMap();
        if (includePlayer) {
            map.putAll(tokens);
        }

        if (includeServer) {
            map.putAll(serverTokens);
        }

        for (Map<String, Function<CommandSource, Text>> customToken : customTokens) {
            map.putAll(customToken);
        }

        return getMessageFromTemplate(template, cs, trimTrailingSpace, playerTokenSplitter, playerTokenMatcher, map);
    }

    // String -> Text parser. Should split on all {{}} tags, but keep the tags in. We can then use the target map
    // to do the replacements!
    public Text getPlayerMessageFromTemplate(String template, CommandSource cs, boolean trimTrailingSpace) {
        return getMessageFromTemplate(template, cs, trimTrailingSpace, playerTokenSplitter, playerTokenMatcher, tokens, serverTokens);
    }

    public Text getServerMessageFromTemplate(String template, CommandSource cs, boolean trimTrailingSpace) {
        return getMessageFromTemplate(template, cs, trimTrailingSpace, serverTokenSplitter, serverTokenMatcher, serverTokens);
    }

    @SafeVarargs
    private final Text getMessageFromTemplate(String template, CommandSource cs, boolean trimTrailingSpace,
                                              Pattern splitter, Pattern matcher, Map<String, Function<CommandSource, Text>>... tokensArray) {
        StyleTuple st = new StyleTuple(TextColors.WHITE, TextStyles.NONE);

        Map<String, Function<CommandSource, Text>> tokens = Maps.newHashMap();
        for (Map<String, Function<CommandSource, Text>> stringFunctionMap : tokensArray) {
            tokens.putAll(stringFunctionMap);
        }

        Text.Builder tb = Text.builder();
        for (String textElement : splitter.split(template)) {
            if (matcher.matcher(textElement).matches()) {
                // If we have a token, do the replacement as specified by the function
                Text message = Text.builder().color(st.colour).style(st.style)
                        .append(tokens.get(textElement.toLowerCase()).apply(cs)).build();
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
                    Text r = TextSerializers.FORMATTING_CODE.deserialize(textElement);
                    tb.append(Text.of(st.colour, st.style, r));
                    st = getLastColourAndStyle(r, st);
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
        StyleTuple st = new StyleTuple(TextColors.WHITE, TextStyles.NONE);
        do {

            // We found a URL. We split on the URL that we have.
            String[] textArray = remaining.split(urlParser.pattern(), 2);
            Text first = Text.builder().color(st.colour).style(st.style)
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
            }

            st = getLastColourAndStyle(first, st);

            // Build the URL
            String url = m.group("url");
            String whiteSpace = m.group("first");
            if (!whiteSpace.isEmpty()) {
                url = String.join("", whiteSpace, url);
            }

            try {
                URL urlObj;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    urlObj = new URL("http://" + url);
                } else {
                    urlObj = new URL(url);
                }

                texts.add(Text.builder(url).color(st.colour).style(st.style)
                        .onHover(TextActions.showText(Util.getTextMessageWithFormat("chat.url.click", url)))
                        .onClick(TextActions.openUrl(urlObj))
                        .build());
            } catch (MalformedURLException e) {
                // URL parsing failed, just put the original text in here.
                if (this.cca == null) {
                    this.cca = plugin.getInjector().getInstance(CoreConfigAdapter.class);
                }

                plugin.getLogger().warn(plugin.getMessageProvider().getMessageWithFormat("chat.url.malformed", url));
                texts.add(Text.builder(url).color(st.colour).style(st.style).build());

                if (this.cca.getNodeOrDefault().isDebugmode()) {
                    e.printStackTrace();
                }
            }
        } while (remaining != null && m.find());

        // Add the last bit.
        if (remaining != null) {
            texts.add(Text.builder().color(st.colour).style(st.style)
                    .append(TextSerializers.FORMATTING_CODE.deserialize(remaining)).build());
        }

        // Join it all together.
        return Text.join(texts);
    }

    private StyleTuple getLastColourAndStyle(Text text, StyleTuple current) {
        List<Text> texts = flatten(text);
        TextColor tc = TextColors.NONE;
        TextStyle ts = TextStyles.NONE;
        for (int i = texts.size() - 1; i > -1; i--) {
            // If we have both a Text Colour and a Text Style, then break out.
            if (tc != TextColors.NONE && ts != TextStyles.NONE) {
                break;
            }
            
            if (tc == TextColors.NONE) {
                tc = texts.get(i).getColor();
            }

            if (ts == TextStyles.NONE) {
                ts = texts.get(i).getStyle();
            }
        }

        if (current == null) {
            return new StyleTuple(tc, ts);
        }

        return new StyleTuple(tc != TextColors.NONE ? tc : current.colour, ts != TextStyles.NONE ? ts : current.style);
    }

    private List<Text> flatten(Text text) {
        List<Text> texts = Lists.newArrayList(text);
        if (!text.getChildren().isEmpty()) {
            text.getChildren().forEach(x -> texts.addAll(flatten(x)));
        }

        return texts;
    }

    private Map<String, Function<CommandSource, Text>> createTokens() {
        Map<String, Function<CommandSource, Text>> t = new HashMap<>();

        t.put("{{name}}", (p) -> Text.of(p.getName()));
        t.put("{{prefix}}", (p) -> getTextFromOption(p, "prefix"));
        t.put("{{suffix}}", (p) -> getTextFromOption(p, "suffix"));
        t.put("{{displayname}}", this::getName);

        return t;
    }

    private Map<String, Function<CommandSource, Text>> createServerTokens() {
        Map<String, Function<CommandSource, Text>> t = new HashMap<>();

        t.put("{{maxplayers}}", p -> Text.of(Sponge.getServer().getMaxPlayers()));
        t.put("{{onlineplayers}}", p -> Text.of(Sponge.getServer().getOnlinePlayers().size()));
        t.put("{{currentworld}}", p -> Text.of(getWorld(p).getName()));
        t.put("{{time}}", p -> Text.of(String.valueOf(Util.getTimeFromTicks(getWorld(p).getProperties().getWorldTime()))));

        return t;
    }

    private World getWorld(CommandSource p) {
        World world;
        if (p instanceof LocatedSource) {
            world = ((LocatedSource) p).getWorld();
        } else {
            world = Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get();
        }

        return world;
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
            return NameUtil.getName((Player)cs, plugin.getUserDataManager());
        }

        return Text.of(cs.getName());
    }

    private static final class StyleTuple {
        private final TextColor colour;
        private final TextStyle style;

        private StyleTuple(TextColor colour, TextStyle style) {
            this.colour = colour;
            this.style = style;
        }
    }
}

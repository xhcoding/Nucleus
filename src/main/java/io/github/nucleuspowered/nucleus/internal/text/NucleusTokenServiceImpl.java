/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class NucleusTokenServiceImpl implements NucleusMessageTokenService {

    private final Map<String, TokenParser> tokenStore = Maps.newHashMap();
    private final Map<String, Tuple<TokenParser, String>> primaryTokenStore = Maps.newHashMap();
    private final NucleusPlugin plugin;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") private final Optional<Text> EMPTY = Optional.empty();

    public NucleusTokenServiceImpl(NucleusPlugin plugin) {
        this.plugin = plugin;
        try {
            Tokens tokens = new Tokens();
            PluginContainer pluginContainer = plugin.getPluginContainer();
            register(pluginContainer, tokens);
            tokens.getTokenNames().forEach(x -> registerPrimaryToken(x.toLowerCase(), pluginContainer, x.toLowerCase()));
        } catch (PluginAlreadyRegisteredException e) {
            e.printStackTrace();
        }
    }

    @Override public void register(PluginContainer pluginContainer, TokenParser textFunction) throws PluginAlreadyRegisteredException {
        Preconditions.checkNotNull(pluginContainer);
        Preconditions.checkNotNull(textFunction);

        if (tokenStore.containsKey(pluginContainer.getId())) {
            throw new PluginAlreadyRegisteredException(pluginContainer);
        }

        tokenStore.put(pluginContainer.getId(), textFunction);
    }

    @Override public boolean unregister(PluginContainer pluginContainer) {
        Preconditions.checkNotNull(pluginContainer, "pluginContainer");
        Preconditions.checkState(!pluginContainer.getId().equalsIgnoreCase(PluginInfo.ID), "Cannot remove Nucleus tokens");
        TokenParser parser = tokenStore.remove(pluginContainer.getId());
        if (parser != null) {
            primaryTokenStore.entrySet().removeIf(x -> x.getValue().getFirst().equals(parser));
            return true;
        }

        return false;
    }

    @Override public boolean registerPrimaryToken(String primaryIdentifier, PluginContainer registeringPlugin, String identiferToMapTo) {
        Preconditions.checkArgument(!primaryIdentifier.matches("^.*[\\s|{}:].*$"), "Token cannot contain spaces or \":|{}\"");
        if (tokenStore.containsKey(registeringPlugin.getId()) && !primaryTokenStore.containsKey(primaryIdentifier.toLowerCase())) {
            // Register!
            primaryTokenStore.put(primaryIdentifier.toLowerCase(), Tuple.of(tokenStore.get(registeringPlugin.getId()),
                    identiferToMapTo.toLowerCase()));
            return true;
        }

        return false;
    }

    @Override public Optional<TokenParser> getTokenParser(String plugin) {
        Preconditions.checkNotNull(plugin, "pluginContainer");
        return Optional.ofNullable(tokenStore.get(plugin.toLowerCase()));
    }

    @Override public Optional<Tuple<TokenParser, String>> getPrimaryTokenParserAndIdentifier(String primaryToken) {
        return Optional.ofNullable(primaryTokenStore.get(primaryToken.toLowerCase()));
    }

    @Override public List<String> getPrimaryTokens() {
        return ImmutableList.copyOf(primaryTokenStore.keySet());
    }

    @Override public Optional<Text> parseToken(String token, CommandSource source, @Nullable Map<String, Object> variables) {
        return getTextFromToken(token, source, variables);
    }

    @Override public boolean registerTokenFormat(String tokenStart, String tokenEnd, String replacement) throws IllegalArgumentException {
        return NucleusTextTemplateFactory.INSTANCE.registerTokenTranslator(tokenStart, tokenEnd, replacement);
    }

    @Override public NucleusTextTemplate createFromString(String string) throws NucleusException {
        try {
            return NucleusTextTemplateFactory.INSTANCE.create(string);
        } catch (Throwable throwable) {
            throw new NucleusException(Text.of("Error creating template."), throwable, NucleusException.ExceptionType.UNKNOWN_ERROR);
        }
    }

    public final Optional<Text> getTextFromToken(String token, CommandSource source, Map<String, Object> variables) {
        token = token.toLowerCase().trim().replace("{{", "").replace("}}", "");
        boolean matches = token.matches(".*:[sp]+$");
        boolean addSpace;
        boolean prependSpace;
        if (matches) {
            Matcher m = Pattern.compile(":([sp]+)$", Pattern.CASE_INSENSITIVE).matcher(token);
            m.find(0);
            String match = m.group(1).toLowerCase();
            addSpace = match.contains("s");
            prependSpace = match.contains("p");

            token = token.replaceAll(":[sp]+$", "");
        } else {
            addSpace = false;
            prependSpace = false;
        }

        try {
            if (token.startsWith("pl:") || token.startsWith("p:")) {
                // Plugin identifers are of the form pl:<pluginid>:<identifier>
                String[] tokSplit = token.split(":", 3);
                if (tokSplit.length < 3) {
                    return EMPTY;
                }

                return applyToken(tokSplit[1], tokSplit[2], source, variables)
                        .map(x -> addSpace ? Text.join(x, Util.NOT_EMPTY) : x)
                        .map(x -> prependSpace ? Text.join(Util.NOT_EMPTY, x) : x);
            } else if (token.startsWith("o:")) { // Option identifier.
                return getTextFromOption(source, token.substring(2), addSpace, prependSpace);
            } else {
                // Standard.
                return applyPrimaryToken(token, source, variables)
                    .map(x -> addSpace ? Text.join(x, Util.NOT_EMPTY) : x)
                    .map(x -> prependSpace ? Text.join(Util.NOT_EMPTY, x) : x);
            }
        } catch (Exception e) {
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }

            return EMPTY;
        }
    }

    private Optional<Text> getTextFromOption(CommandSource cs, String option, boolean addSpace, boolean prependSpace) {
        if (cs instanceof Player) {
            Optional<String> os = Util.getOptionFromSubject(cs, option);
            if (os.isPresent() && !os.get().isEmpty()) {
                String s = os.get();
                if (addSpace) {
                    s += " ";
                }

                if (prependSpace) {
                    s = " " + s;
                }

                return Optional.of(TextSerializers.FORMATTING_CODE.deserialize(s));
            }
        }

        return Optional.empty();
    }

    public Tokens getNucleusTokenParser() {
        return ((Tokens)tokenStore.get("nucleus"));
    }
}

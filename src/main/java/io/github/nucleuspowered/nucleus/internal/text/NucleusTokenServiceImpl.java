/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;

import java.util.Map;
import java.util.Optional;

public class NucleusTokenServiceImpl implements NucleusMessageTokenService {

    private final Map<String, TokenParser> tokenStore = Maps.newHashMap();
    private final Map<String, Tuple<TokenParser, String>> primaryTokenStore = Maps.newHashMap();

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

    public Tokens getNucleusTokenParser() {
        return ((Tokens)tokenStore.get("nucleus"));
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Allows plugins to register their own tokens for use in templated messages.
 */
public interface NucleusMessageTokenService {

    /**
     * Registers a token for the given {@link PluginContainer}. The token will have the format <code>{{pl:pluginid:identifier(:s)}}</code>, the <code>:s</code>
     * is optional.
     *
     * @param pluginContainer The {@link PluginContainer} of the plugin.
     * @param parser The {@link TokenParser} that recieves the identifier from the token, along with any contextual variables and
     *               the target {@link MessageReceiver} of the message.
     * @throws PluginAlreadyRegisteredException Thrown if the token has been registered previously.
     */
    void register(PluginContainer pluginContainer, TokenParser parser) throws PluginAlreadyRegisteredException;

    /**
     * Unregisters the {@link TokenParser} for a plugin.
     *
     * @param pluginContainer The {@link PluginContainer} of the plugin.
     * @return <code>true</code> if successful.
     */
    boolean unregister(PluginContainer pluginContainer);

    /**
     * Gets the function applied for the specified {@link PluginContainer}, if it exists.
     *
     * @param pluginContainer The {@link PluginContainer} of the pluginContainer that registered the token.
     * @return The {@link TokenParser} that is run for the token, if it exists.
     */
    default Optional<TokenParser> getTokenParser(PluginContainer pluginContainer) {
        Preconditions.checkNotNull(pluginContainer, "pluginContainer");
        return getTokenParser(pluginContainer.getId());
    }

    /**
     * Gets the function applied for the specified plugin, if it exists.
     *
     * @param plugin The ID of the plugin that registered the token.
     * @return The {@link TokenParser} that is run for the token, if it exists.
     */
    Optional<TokenParser> getTokenParser(String plugin);

    default Optional<Text> applyToken(String plugin, String token, CommandSource source) {
        return applyToken(plugin, token, source, Maps.newHashMap());
    }

    /**
     * Gets the result of a token's registered {@link Function} on a {@link CommandSource}
     *
     * @param plugin The ID of the plugin that registered the token.
     * @param token The ID of the token.
     * @param source The {@link MessageReceiver} to perform the operation with.
     * @param variables The variables that could be used in the token.
     * @return The {@link Text}, if any.
     */
    default Optional<Text> applyToken(String plugin, String token, CommandSource source, Map<String, Object> variables) {
        Optional<TokenParser> tokenFunction = getTokenParser(plugin);
        if (tokenFunction.isPresent()) {
            return tokenFunction.get().parse(token, source, variables);
        }

        return Optional.empty();
    }

    /**
     * Uses Nucleus' parser to format a string that uses Minecraft colour codes.
     *
     * @param input The input.
     * @param source The {@link CommandSource} that will view the message.
     * @return The {@link Text} that represents the output.
     */
    default Text formatAmpersandEncodedStringWithTokens(String input, CommandSource source) {
        return formatAmpersandEncodedStringWithTokens(input, source, Maps.newHashMap());
    }

    /**
     * Uses Nucleus' parser to format a string that uses Minecraft colour codes.
     *
     * @param input The input.
     * @param source The {@link CommandSource} that will view the message.
     * @param variables Any variables to be provided to the text.
     * @return The {@link Text} that represents the output.
     */
    Text formatAmpersandEncodedStringWithTokens(String input, CommandSource source, Map<String, Object> variables);

    @FunctionalInterface
    interface TokenParser {

        /**
         * Parses a plugin's token and returns {@link Text}, if any.
         *
         * @param tokenInput The identifier for the token.
         * @param source The {@link CommandSource} that will be viewing the output of this token.
         * @param variables A map of variable names to variable objects. Consult documentation for the
         *                  variables that might be caused by an event or command.
         * @return The {@link Text} to display, or {@link Optional#empty()} if the token cannot be parsed.
         */
        @Nonnull
        Optional<Text> parse(String tokenInput, CommandSource source, Map<String, Object> variables);
    }
}

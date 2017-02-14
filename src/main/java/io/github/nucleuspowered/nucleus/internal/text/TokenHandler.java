/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public class TokenHandler {

    private final NucleusPlugin plugin;
    private boolean registered = false;
    private NucleusTokenServiceImpl service = null;

    private final Text SPACE = Text.of(" ");

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") private final Optional<Text> EMPTY = Optional.empty();

    public TokenHandler(NucleusPlugin plugin) {
        this.plugin = plugin;
    }

    public final void register(NucleusTokenServiceImpl service) {
        Preconditions.checkState(!registered);
        this.service = service;
        try {
            Tokens tokens = new Tokens();
            PluginContainer pluginContainer = plugin.getPluginContainer();
            service.register(pluginContainer, tokens);
            tokens.getTokenNames().forEach(x -> service.registerPrimaryToken(x.toLowerCase(), pluginContainer, x.toLowerCase()));
        } catch (PluginAlreadyRegisteredException e) {
            e.printStackTrace();
        }

        registered = true;
    }

    public final Optional<Text> getTextFromToken(String token, CommandSource source, Map<String, Object> variables) {
        token = token.toLowerCase().trim().replace("{{", "").replace("}}", "");
        boolean addSpace = token.endsWith(":s");
        if (addSpace) {
            token = token.substring(0, token.length() - 2);
        }

        try {
            if (token.startsWith("pl:") || token.startsWith("p:")) {
                // Plugin identifers are of the form pl:<pluginid>:<identifier>
                String[] tokSplit = token.split(":", 3);
                if (tokSplit.length < 3) {
                    return EMPTY;
                }

                return getTextFromPluginToken(tokSplit[1], tokSplit[2], source, addSpace, variables);
            } else if (token.startsWith("o:")) { // Option identifier.
                return getTextFromOption(source, token.substring(2), addSpace);
            } else {
                // Standard.
                return getTextFromPluginToken(null, token, source, addSpace, variables);
            }
        } catch (Exception e) {
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }

            return EMPTY;
        }
    }

    private Optional<Text> getTextFromPluginToken(@Nullable String pluginId, String token, CommandSource source, boolean addSpace,
            Map<String, Object> variables) {
        if (service != null) {
            Optional<Text> opt =
                pluginId == null ?
                    service.applyPrimaryToken(token, source, variables) : service.applyToken(pluginId, token, source, variables);
            if (opt.isPresent() && addSpace) {
                return Optional.of(opt.get().toBuilder().append(SPACE).build());
            }

            return opt;
        }

        return EMPTY;
    }

    private Optional<Text> getTextFromOption(CommandSource cs, String option, boolean addSpace) {
        if (cs instanceof Player) {
            Optional<String> os = Util.getOptionFromSubject(cs, option);
            if (os.isPresent() && !os.get().isEmpty()) {
                String s = os.get();
                if (addSpace) {
                    s += " ";
                }

                return Optional.of(TextSerializers.FORMATTING_CODE.deserialize(s));
            }
        }

        return Optional.of(Text.EMPTY);
    }
}

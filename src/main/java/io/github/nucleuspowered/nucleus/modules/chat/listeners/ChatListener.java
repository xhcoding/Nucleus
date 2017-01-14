/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.listeners;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.util.NucleusIgnorableChatChannel;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.chat.ChatModule;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatTemplateConfig;
import io.github.nucleuspowered.nucleus.modules.chat.util.TemplateUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A listener that modifies all chat messages. Uses the
 * {@link io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService}, which
 * should be used if tokens need to be registered.
 */
@ConditionalListener(ChatListener.Test.class)
public class ChatListener extends ListenerBase {

    private static final Pattern prefixPattern = Pattern.compile("^\\s*<[a-zA-Z0-9_]+>\\s*$");
    private static final String prefix = PermissionRegistry.PERMISSIONS_PREFIX + "chat.";

    // Order is important here!
    private static final Map<String, String> permissionToDesc = Maps.newHashMap();
    private static final Map<String[], Function<String, String>> replacements = createReplacements();

    private static Map<String[], Function<String, String>> createReplacements() {
        Map<String[], Function<String, String>> t = new HashMap<>();

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();

        BiFunction<String, String, String> fss = (key, s) -> s.replaceAll("[&]+[" + key.toLowerCase() + key.toUpperCase() + "]", "");
        NameUtil.getColours().forEach((key, value) -> {
            t.put(new String[]{prefix + "colour." + value.getName(), prefix + "color." + value.getName()}, s -> fss.apply(key.toString(), s));
            permissionToDesc.put(prefix + "colour." + value.getName(), mp.getMessageWithFormat("permission.chat.colourspec", value.getName().toLowerCase(), key.toString()));
            permissionToDesc.put(prefix + "color." + value.getName(), mp.getMessageWithFormat("permission.chat.colorspec", value.getName().toLowerCase(), key.toString()));
        });

        NameUtil.getStyles().entrySet().stream().filter(x -> x.getKey().getFirst() != 'k').forEach((k) -> {
            t.put(new String[] { prefix + "style." + k.getKey().getSecond().toLowerCase() }, s -> fss.apply(k.getKey().getFirst().toString(), s));
            permissionToDesc.put(prefix + "style." + k.getKey().getSecond().toLowerCase(),
                mp.getMessageWithFormat("permission.chat.stylespec", k.getKey().getSecond().toLowerCase(), k.getKey().getFirst().toString()));
        });

        t.put(new String[] { prefix + "magic" }, s -> s.replaceAll("[&]+[kK]", ""));

        return t;
    }

    public static String stripPermissionless(Subject source, String message) {
        for (Map.Entry<String[],  Function<String, String>> r : replacements.entrySet()) {
            // If we don't have the required permission...
            if (Arrays.stream(r.getKey()).noneMatch(source::hasPermission)) {
                // ...strip the codes.
                message = r.getValue().apply(message);
            }
        }

        return message;
    }

    // --- Listener Proper
    private final ChatConfigAdapter cca;
    private final ChatUtil chatUtil;
    private final TemplateUtil templateUtil;

    @Inject
    public ChatListener(ChatUtil chatUtil, ChatConfigAdapter cca, TemplateUtil templateUtil) {
        this.chatUtil = chatUtil;
        this.cca = cca;
        this.templateUtil = templateUtil;
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = new HashMap<>();
        mp.put(prefix + "color", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.chat.color"), SuggestedLevel.ADMIN));
        mp.put(prefix + "colour", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.chat.colour"), SuggestedLevel.ADMIN));
        mp.put(prefix + "style", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.chat.style"), SuggestedLevel.ADMIN));
        mp.put(prefix + "magic", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.chat.magic"), SuggestedLevel.ADMIN));
        mp.put(prefix + "url", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.chat.urls"), SuggestedLevel.ADMIN));
        permissionToDesc.forEach((k, v) -> mp.put(k, new PermissionInformation(v, SuggestedLevel.ADMIN)));
        return mp;
    }

    // We do this first so that other plugins can alter it later if needs be.
    @Listener(order = Order.EARLY)
    public void onPlayerChat(MessageChannelEvent.Chat event, @Root Player player) {
        if (event.getChannel().isPresent() && event.getChannel().get() instanceof NucleusIgnorableChatChannel) {
            // Staff chat. Not interested in applying these transforms.
            return;
        }

        MessageEvent.MessageFormatter eventFormatter = event.getFormatter();
        ChatConfig config = cca.getNodeOrDefault();
        Text rawMessage = eventFormatter.getBody().isEmpty() ? event.getRawMessage() : eventFormatter.getBody().toText();

        Text prefix = Text.EMPTY;

        // Avoid adding <name>.
        if (!config.isOverwriteEarlyPrefixes() && !prefixPattern.matcher(eventFormatter.getHeader().toText().toPlain()).matches()) {
            prefix = eventFormatter.getHeader().toText();
        }

        Text footer = config.isOverwriteEarlySuffixes() ? Text.EMPTY : event.getFormatter().getFooter().toText();

        final ChatTemplateConfig ctc;
        if (config.isUseGroupTemplates()) {
            ctc = templateUtil.getTemplate(player);
        } else {
            ctc = config.getDefaultTemplate();
        }

        event.setMessage(
                Text.join(prefix, chatUtil.getMessageFromTemplate(ctc.getPrefix(), player, true)),
                config.isModifyMainMessage() ? useMessage(player, rawMessage, ctc) : rawMessage,
                Text.join(footer, chatUtil.getMessageFromTemplate(ctc.getSuffix(), player, false)));
    }

    private Text useMessage(Player player, Text rawMessage, ChatTemplateConfig chatTemplateConfig) {
        String m = stripPermissionless(player, TextSerializers.FORMATTING_CODE.serialize(rawMessage));

        Text result;
        if (player.hasPermission(prefix + "url")) {
            result = chatUtil.addUrlsToAmpersandFormattedString(m);
        } else {
            result = TextSerializers.FORMATTING_CODE.deserialize(m);
        }

        String chatcol = getOption(player, "chatcolour", "chatcolor").orElseGet(chatTemplateConfig::getChatcolour);
        String chatstyle = getOption(player, "chatstyle").orElseGet(chatTemplateConfig::getChatstyle);

        NameUtil nu = plugin.getNameUtil();
        return Text.of(nu.getColourFromString(chatcol), nu.getTextStyleFromString(chatstyle), result);
    }

    private Optional<String> getOption(Player player, String... option) {
        Optional<String> optional = Util.getOptionFromSubject(player, option);
        if (optional.isPresent() && !optional.get().isEmpty()) {
            return optional;
        }

        return Optional.empty();
    }

    public static class Test implements Predicate<Nucleus> {

        @Override
        public boolean test(Nucleus nucleus) {
            return nucleus.getConfigValue(ChatModule.ID, ChatConfigAdapter.class, ChatConfig::isModifychat).orElse(false);
        }
    }
}

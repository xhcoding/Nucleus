/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ChatListener extends ListenerBase {

    private final String prefix = PermissionRegistry.PERMISSIONS_PREFIX + "chat.";

    private final Map<String[], Function<String, String>> replacements;

    @Inject private ChatConfigAdapter cca;
    @Inject private ChatUtil chatUtil;

    // Zero args for the injector
    public ChatListener() {
        replacements = createReplacements();
    }

    @Override
    protected Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = new HashMap<>();
        mp.put(prefix + "color", new PermissionInformation(Util.getMessageWithFormat("permission.chat.color"), SuggestedLevel.ADMIN));
        mp.put(prefix + "colour", new PermissionInformation(Util.getMessageWithFormat("permission.chat.colour"), SuggestedLevel.ADMIN));
        mp.put(prefix + "style", new PermissionInformation(Util.getMessageWithFormat("permission.chat.style"), SuggestedLevel.ADMIN));
        mp.put(prefix + "magic", new PermissionInformation(Util.getMessageWithFormat("permission.chat.magic"), SuggestedLevel.ADMIN));
        // TODO: URLs.
        // mp.put(prefix + "url", new PermissionInformation(Util.getMessageWithFormat("permission.chat.urls"), SuggestedLevel.ADMIN));
        return super.getPermissions();
    }

    private Map<String[], Function<String, String>> createReplacements() {
        Map<String[], Function<String, String>> t = new HashMap<>();

        t.put(new String[] { prefix + "colour", prefix + "color" }, s -> s.replaceAll("&[0-9a-f]", ""));
        t.put(new String[] { prefix + "style" }, s -> s.replaceAll("&[lmno]", ""));
        t.put(new String[] { prefix + "magic" }, s -> s.replaceAll("&k", ""));

        return t;
    }

    private Text useMessage(Player player, Text rawMessage) {
        String m = rawMessage.toPlain();

        for (Map.Entry<String[],  Function<String, String>> r : replacements.entrySet()) {
            // If we don't have the required permission...
            if (Arrays.asList(r.getKey()).stream().noneMatch(player::hasPermission)) {
                // ...strip the codes.
                m = r.getValue().apply(m);
            }
        }

        return TextSerializers.formattingCode('&').deserialize(m);
    }

    // We do this first so that other plugins can alter it later if needs be.
    @Listener(order = Order.EARLY)
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        ChatConfig config = cca.getNodeOrDefault();
        if (!config.isModifychat()) {
            return;
        }

        Text rawMessage = event.getRawMessage();
        event.setMessage(
                chatUtil.getFromTemplate(config.getTemplate().getPrefix(), player, true),
                useMessage(player, rawMessage),
                chatUtil.getFromTemplate(config.getTemplate().getSuffix(), player, false));
    }


}

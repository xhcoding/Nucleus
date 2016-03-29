/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserLoaderService;
import io.github.nucleuspowered.nucleus.modules.message.events.InternalNucleusMessageEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageHandler {

    private final Map<UUID, UUID> messagesReceived = Maps.newHashMap();
    private final Text me = Util.getTextMessageWithFormat("message.me");

    public void update(UUID from, UUID to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        messagesReceived.put(to, from);
    }

    public Optional<CommandSource> getPlayerToReplyTo(UUID from) {
        Preconditions.checkNotNull(from);
        UUID to = messagesReceived.get(from);
        if (to == null) {
            return Optional.empty();
        }

        if (to.equals(Util.consoleFakeUUID)) {
            return Optional.of(Sponge.getServer().getConsole());
        }

        return Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.getUniqueId().equals(to)).map(y -> (CommandSource) y).findFirst();
    }

    public void clearUUID(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        messagesReceived.remove(uuid);
        messagesReceived.entrySet().removeIf(f -> f.getValue().equals(uuid));
    }

    public boolean replyMessage(CommandSource sender, String message) {
        Optional<CommandSource> cs = getPlayerToReplyTo(getUUID(sender));
        if (cs.isPresent()) {
            return sendMessage(sender, cs.get(), message);
        }

        sender.sendMessage(Util.getTextMessageWithFormat("message.noreply"));
        return false;
    }

    public boolean sendMessage(CommandSource sender, CommandSource receiver, String message) {
        Text nameOfSender = getName(sender);
        Text nameOfReceiver = getName(receiver);

        NucleusUserLoaderService qs = Sponge.getServiceManager().provideUnchecked(NucleusUserLoaderService.class);

        // Message is about to be sent. Send the event out. If canceled, then
        // that's that.
        if (Sponge.getEventManager().post(new InternalNucleusMessageEvent(sender, receiver, message))) {
            sender.sendMessage(Util.getTextMessageWithFormat("message.cancel"));
            return false;
        }

        // Social Spies.
        List<MessageReceiver> lm =
                qs.getOnlineUsers().stream().filter(x -> !getUUID(sender).equals(x.getUniqueID()) && !getUUID(receiver).equals(x.getUniqueID()))
                        .filter(NucleusUser::isSocialSpy).map(x -> x.getUser().getPlayer().get()).collect(Collectors.toList());

        if (getUUID(sender) != Util.consoleFakeUUID && getUUID(receiver) != Util.consoleFakeUUID) {
            lm.add(Sponge.getServer().getConsole());
        }

        MessageChannel mc = MessageChannel.fixed(lm);
        sender.sendMessage(constructMessage(me, nameOfReceiver, message));
        receiver.sendMessage(constructMessage(nameOfSender, me, message));
        mc.send(constructSSMessage(nameOfSender, nameOfReceiver, message));
        update(getUUID(sender), getUUID(receiver));
        return true;
    }

    private UUID getUUID(CommandSource sender) {
        return sender instanceof Player ? ((Player) sender).getUniqueId() : Util.consoleFakeUUID;
    }

    private Text getName(CommandSource src) {
        if (!(src instanceof Player)) {
            return Text.builder(src.getName()).color(TextColors.LIGHT_PURPLE).onClick(TextActions.suggestCommand("/msg - ")).build();
        }

        return NameUtil.getNameFromCommandSource(src).toBuilder().onClick(TextActions.suggestCommand("/msg " + src.getName() + " ")).build();
    }

    private Text constructSSMessage(Text from, Text to, String message) {
        return Text.builder().append(Text.of(TextColors.GRAY, "[")).append(Util.getTextMessageWithFormat("message.socialspy"))
                .append(Text.of(TextColors.GRAY, "] ")).append(from).append(Text.of(TextColors.GRAY, " -> ")).append(to)
                .append(Text.of(TextColors.GRAY, ": ")).append(Text.of(TextColors.GRAY, message)).build();
    }

    private Text constructMessage(Text from, Text to, String message) {
        return Text.of(from).toBuilder().append(Text.of(TextColors.GRAY, " -> ")).append(to).append(Text.of(TextColors.GRAY, ": "))
                .append(Text.of(TextColors.GRAY, message)).build();
    }
}

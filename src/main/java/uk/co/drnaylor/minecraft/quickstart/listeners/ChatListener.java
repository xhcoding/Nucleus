package uk.co.drnaylor.minecraft.quickstart.listeners;

import com.google.inject.Inject;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;

@Modules(PluginModule.CHAT)
public class ChatListener {

    @Inject
    private MainConfig config;

    @Listener(order = Order.LATE)
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        if (!config.getModifyChat()) {
            return;
        }

        Text rawMessage = event.getRawMessage();
    }

    private Text getPrefix(Player player) {
        return Text.EMPTY;
    }

    private Text getSuffix(Player player) {
        return Text.EMPTY;
    }

    private Text getDisplayName(Player player) {
        return Util.getName(player);
    }
}

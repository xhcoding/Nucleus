package uk.co.drnaylor.minecraft.quickstart.listeners;

import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.services.MailHandler;

import java.util.concurrent.TimeUnit;

@Modules(PluginModule.MAILS)
public class MailListener extends ListenerBase {
    @Inject private MailHandler handler;
    @Inject private Game game;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        game.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
            int mailCount = handler.getMail(event.getTargetEntity()).size();
            if (mailCount > 0) {
                event.getTargetEntity().sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("mail.login",
                        String.valueOf(mailCount))));
                event.getTargetEntity().sendMessage(Text.builder()
                        .append(Text.builder("/mail").color(TextColors.AQUA).style(TextStyles.UNDERLINE).onClick(TextActions.runCommand("/mail"))
                                .onHover(TextActions.showText(Text.of("Click here to read your mail."))).build())
                        .append(Text.of(TextColors.YELLOW, " " + Util.getMessageWithFormat("mail.toread") + " "))
                        .append(Text.builder("/mail clear").color(TextColors.AQUA).style(TextStyles.UNDERLINE).onClick(TextActions.runCommand("/mail clear"))
                                .onHover(TextActions.showText(Text.of("Click here to delete your mail."))).build())
                        .append(Text.of(TextColors.YELLOW, " " + Util.getMessageWithFormat("mail.toclear")))
                        .build()
                );
            }
        }, 1, TimeUnit.SECONDS);
    }
}

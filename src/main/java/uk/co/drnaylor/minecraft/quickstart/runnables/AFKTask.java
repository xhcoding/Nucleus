package uk.co.drnaylor.minecraft.quickstart.runnables;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.handlers.AFKHandler;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class AFKTask implements Consumer<Task> {
    private final QuickStart plugin;

    public AFKTask(QuickStart plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(Task task) {
        // Check to see if anyone has gone across the timeout.
        AFKHandler a = plugin.getAfkHandler();
        a.purgeNotOnline();

        MainConfig c = plugin.getConfig(MainConfig.class).get();

        // AFK time
        if (c.getAfkTime() > 0) {
            List<UUID> afking = plugin.getAfkHandler().checkForAfk(c.getAfkTime());
            if (!afking.isEmpty()) {
                Sponge.getServer().getOnlinePlayers().stream().filter(x -> !x.hasPermission(QuickStart.PERMISSIONS_PREFIX + "afk.exempt") &&
                            afking.contains(x.getUniqueId())).map(Util::getName)
                        .forEach(x -> MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", x, TextColors.GRAY, " " + Util.messageBundle.getString("afk.toafk"))));
            }
        }

        // Kick after AFK time
        if (c.getAfkTimeToKick() > 0) {
            List<UUID> afking = plugin.getAfkHandler().checkForAfkKick(c.getAfkTimeToKick());
            if (!afking.isEmpty()) {
                Sponge.getServer().getOnlinePlayers().stream().filter(x -> !x.hasPermission(QuickStart.PERMISSIONS_PREFIX + "afk.exemptkick") &&
                        afking.contains(x.getUniqueId()))
                        .forEach(x -> {
                            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> x.kick(Text.of(Util.messageBundle.getString("afk.kickreason"))));
                            MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", Util.getName(x), TextColors.GRAY, " " + Util.messageBundle.getString("afk.kickedafk")));
                        });
            }
        }
    }
}

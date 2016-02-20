/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.runnables;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.TaskBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.services.AFKHandler;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Modules(PluginModule.AFK)
public class AFKTask implements TaskBase {
    @Inject private QuickStart plugin;

    @Override
    public void accept(Task task) {
        // Check to see if anyone has gone across the timeout.
        AFKHandler a = plugin.getAfkHandler();
        a.purgeNotOnline();

        MainConfig c = plugin.getConfig(ConfigMap.MAIN_CONFIG).get();

        // AFK time
        if (c.getAfkTime() > 0) {
            List<UUID> afking = plugin.getAfkHandler().checkForAfk(c.getAfkTime());
            if (!afking.isEmpty()) {
                Sponge.getServer().getOnlinePlayers().stream().filter(x -> !x.hasPermission(PermissionUtil.PERMISSIONS_PREFIX + "afk.exempt") &&
                            afking.contains(x.getUniqueId())).map(Util::getName)
                        .forEach(x -> MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", x, TextColors.GRAY, " " + Util.getMessageWithFormat("afk.toafk"))));
            }
        }

        // Kick after AFK time
        if (c.getAfkTimeToKick() > 0) {
            List<UUID> afking = plugin.getAfkHandler().checkForAfkKick(c.getAfkTimeToKick());
            if (!afking.isEmpty()) {
                Sponge.getServer().getOnlinePlayers().stream().filter(x -> !x.hasPermission(PermissionUtil.PERMISSIONS_PREFIX + "afk.exemptkick") &&
                        afking.contains(x.getUniqueId()))
                        .forEach(x -> {
                            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> x.kick(Text.of(Util.getMessageWithFormat("afk.kickreason"))));
                            MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", Util.getName(x), TextColors.GRAY, " " + Util.getMessageWithFormat("afk.kickedafk")));
                        });
            }
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public int secondsPerRun() {
        return 2;
    }
}

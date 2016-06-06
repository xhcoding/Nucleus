/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.runnables;

import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.afk.commands.AFKCommand;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.channel.MessageChannel;

import javax.inject.Inject;
import java.util.*;

public class AFKTask extends TaskBase {

    @Inject private Nucleus plugin;
    @Inject private PermissionRegistry permissionRegistry;
    @Inject private AFKConfigAdapter afkconfiga;
    @Inject private AFKHandler handler;
    private CommandPermissionHandler afkService = null;

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put(PermissionRegistry.PERMISSIONS_PREFIX + "afk.exempt.toggle",
                new PermissionInformation(Util.getMessageWithFormat("permission.afk.exempt.toggle"), SuggestedLevel.NONE));
        m.put(PermissionRegistry.PERMISSIONS_PREFIX + "afk.exempt.kick",
                new PermissionInformation(Util.getMessageWithFormat("permission.afk.exempt.kick"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public void accept(Task task) {
        // Don't run the task until we have a permission service.
        if (afkService == null) {
            Optional<CommandPermissionHandler> ops = permissionRegistry.getService(AFKCommand.class);
            if (!ops.isPresent()) {
                return;
            }

            afkService = ops.get();
        }

        // Check to see if anyone has gone across the timeout.
        handler.purgeNotOnline();

        AFKConfig config = afkconfiga.getNodeOrDefault();

        // AFK time
        if (config.getAfkTime() > 0) {
            handler.checkForAfk(config.getAfkTime());
        }

        // Kick after AFK time
        if (config.getAfkTimeToKick() > 0) {
            List<UUID> afking = handler.checkForAfkKick(config.getAfkTimeToKick());
            if (!afking.isEmpty()) {
                Sponge.getServer().getOnlinePlayers().stream()
                    .filter(x -> !x.hasPermission(afkService.getPermissionWithSuffix("exempt.kick")) && afking.contains(x.getUniqueId()))
                    .forEach(x -> {
                        Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> x.kick(Util.getTextMessageWithFormat("afk.kickreason")));
                        MessageChannel.TO_ALL
                                .send(Util.getTextMessageWithFormat("afk.kickedafk", NameUtil.getSerialisedName(x)));
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

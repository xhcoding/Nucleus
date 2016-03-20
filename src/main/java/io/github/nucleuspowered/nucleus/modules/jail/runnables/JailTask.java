/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.runnables;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import java.util.Collection;

public class JailTask extends TaskBase {
    @Inject private Nucleus plugin;
    @Inject private JailHandler jailHandler;

    @Override
    public void accept(Task task) {
        Collection<Player> pl = Sponge.getServer().getOnlinePlayers();
        UserConfigLoader ucl = plugin.getUserLoader();
        pl.stream().map(x -> {
            try {
                return ucl.getUser(x);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).filter(x -> x == null || x.getJailData().isPresent()).forEach(x -> Util.testForEndTimestamp(x.getJailData(), () -> jailHandler.unjailPlayer(x.getUser())));
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

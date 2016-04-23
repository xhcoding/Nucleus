/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.runnables;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Temporary solution until we can check for feeding levels to drop.
 */
public class GodRunnable extends TaskBase {

    @Inject private Nucleus plugin;
    @Inject private UserConfigLoader ucl;
    @Inject private CoreConfigAdapter cca;

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public int secondsPerRun() {
        return 5;
    }

    @Override
    public void accept(Task task) {
        Collection<Player> cp = Sponge.getServer().getOnlinePlayers();
        List<Player> toFeed = cp.stream().filter(this::isInvulnerable).collect(Collectors.toList());
        if (!toFeed.isEmpty()) {
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> toFeed.forEach(p -> p.offer(Keys.FOOD_LEVEL, Integer.MAX_VALUE)));
        }
    }

    private boolean isInvulnerable(Player pl) {
        try {
            return ucl.getUser(pl).isInvulnerable();
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }

            return false;
        }
    }
}

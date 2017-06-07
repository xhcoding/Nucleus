/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.runnables;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.misc.datamodules.InvulnerabilityUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Temporary solution until we can check for feeding levels to drop.
 */
public class GodRunnable extends TaskBase {

    @Inject private NucleusPlugin plugin;
    @Inject private UserDataManager ucl;

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(5, ChronoUnit.SECONDS);
    }

    @Override
    public void accept(Task task) {
        Collection<Player> cp = Sponge.getServer().getOnlinePlayers();
        List<Player> toFeed = cp.stream().filter(x -> ucl.getUser(x)
                .map(y -> y.get(InvulnerabilityUserDataModule.class).isInvulnerable()).orElse(false))
                .collect(Collectors.toList());
        if (!toFeed.isEmpty()) {
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> toFeed.forEach(p -> p.offer(Keys.FOOD_LEVEL, Integer.MAX_VALUE)));
        }
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.protection.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.protection.ProtectionModule;
import io.github.nucleuspowered.nucleus.modules.protection.config.ProtectionConfig;
import io.github.nucleuspowered.nucleus.modules.protection.config.ProtectionConfigAdapter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.util.function.Predicate;

@ConditionalListener(CollisionListener.Condition.class)
public class CollisionListener extends ListenerBase {

    @Listener
    public void onCollision(CollideEntityEvent event, @Root Player player) {
        event.filterEntities(x -> !(x instanceof Player));
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            return nucleus.getConfigValue(ProtectionModule.ID, ProtectionConfigAdapter.class, ProtectionConfig::isDisablePlayerCollisions).orElse(false);
        }
    }
}

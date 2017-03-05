/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.events;

import io.github.nucleuspowered.nucleus.api.events.NucleusKitEvent;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Instant;
import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
public abstract class KitEvent extends AbstractEvent implements NucleusKitEvent {

    public static class PreRedeem extends KitEvent implements NucleusKitEvent.Redeem.Pre {

        private final Cause cause;
        private final String name;
        private final Kit kit;
        @Nullable private final Instant lastTime;
        private final Player targetPlayer;

        @Nullable private Text cancelMessage = null;
        private boolean isCancelled;

        public PreRedeem(Cause cause, @Nullable Instant lastTime, String name, Kit kit, Player targetPlayer) {
            this.cause = cause;
            this.name = name;
            this.kit = kit;
            this.targetPlayer = targetPlayer;
            this.lastTime = lastTime;
        }

        @Override public Optional<Instant> getLastRedeemedTime() {
            return Optional.ofNullable(lastTime);
        }

        @Override public String getName() {
            return this.name;
        }

        @Override public Kit getRedeemedKit() {
            return this.kit;
        }

        @Override public boolean isCancelled() {
            return this.isCancelled;
        }

        @Override public void setCancelled(boolean cancel) {
            this.isCancelled = cancel;
        }

        @Override public Player getTargetEntity() {
            return targetPlayer;
        }

        @Override public Cause getCause() {
            return cause;
        }

        @Override public Optional<Text> getCancelMessage() {
            return Optional.ofNullable(this.cancelMessage);
        }

        @Override public void setCancelMessage(@Nullable Text message) {
            this.cancelMessage = message;
        }
    }

    public static class PostRedeem extends KitEvent implements NucleusKitEvent.Redeem.Post {

        private final Cause cause;
        private final String name;
        private final Kit kit;
        @Nullable private final Instant lastTime;
        private final Player targetPlayer;

        public PostRedeem(Cause cause, @Nullable Instant lastTime, String name, Kit kit, Player targetPlayer) {
            this.cause = cause;
            this.name = name;
            this.kit = kit;
            this.targetPlayer = targetPlayer;
            this.lastTime = lastTime;
        }

        @Override public Optional<Instant> getLastRedeemedTime() {
            return Optional.ofNullable(lastTime);
        }

        @Override public String getName() {
            return this.name;
        }

        @Override public Kit getRedeemedKit() {
            return this.kit;
        }

        @Override public Player getTargetEntity() {
            return targetPlayer;
        }

        @Override public Cause getCause() {
            return cause;
        }
    }
}

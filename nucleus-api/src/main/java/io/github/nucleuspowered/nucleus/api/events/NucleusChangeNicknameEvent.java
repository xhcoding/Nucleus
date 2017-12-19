/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.events;

import io.github.nucleuspowered.nucleus.api.annotations.MightOccurAsync;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.user.TargetUserEvent;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * Fired when a player requests or deletes a nickname.
 */
@MightOccurAsync
public interface NucleusChangeNicknameEvent extends Cancellable, TargetUserEvent {

    /**
     * The previous nickname for the {@link #getTargetUser()}
     *
     * @return The previous nickname.
     */
    Optional<Text> getPreviousNickname();

    /**
     * The new nickname for the {@link #getTargetUser()}
     *
     * @return The nickname, or the player name if no nickname.
     * @deprecated Use {@link #getNickname()} instead
     */
    @Deprecated
    Text getNewNickname();

    /**
     * The new nickname, if any, for the {@link #getTargetUser()}
     *
     * @return The nickname, if any is given
     */
    Optional<Text> getNickname();
}

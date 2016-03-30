/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;

/**
 * @deprecated Deprecated in favour of {@link CommandBase}
 */
@Deprecated
public abstract class OldCommandBase<T extends CommandSource> extends AbstractCommand<T> {

     /**
     * Builds the common base for the CommandSpec.
     *
     * @return The uncompleted {@link org.spongepowered.api.command.spec.CommandSpec.Builder}
     */
    protected final CommandSpec.Builder getSpecBuilderBase() {
        Preconditions.checkState(permissions != null);
        CommandSpec.Builder cb = CommandSpec.builder().executor(this);

        if (!permissions.isPassthrough()) {
            cb.permission(permissions.getBase());
        }

        return cb;
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.modules;

import io.github.nucleuspowered.nucleus.internal.command.ContinueMode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

public interface CommandCheck {

    ContinueMode check(CommandSource source, CommandContext args);

    void complete(CommandSource source, CommandContext args);

    void failed(CommandSource source, CommandContext args);
}

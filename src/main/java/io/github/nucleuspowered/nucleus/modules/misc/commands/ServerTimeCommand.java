/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@Permissions
@RegisterCommand({"servertime", "realtime"})
@NonnullByDefault
public class ServerTimeCommand extends AbstractCommand<CommandSource> {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        src.sendMessage(
            this.plugin.getMessageProvider().getTextMessageWithFormat("command.servertime.time", dtf.format(LocalDateTime.now()))
        );

        return CommandResult.success();
    }
}

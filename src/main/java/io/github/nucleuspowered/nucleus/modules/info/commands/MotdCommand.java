/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.info.InfoModule;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.info.handlers.InfoHelper;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand("motd")
public class MotdCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    @Inject private ChatUtil chatUtil;
    @Inject private InfoConfigAdapter infoConfigAdapter;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<TextFileController> otfc = plugin.getTextFileController(InfoModule.MOTD_KEY);
        if (!otfc.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.motd.nocontroller"));
            return CommandResult.empty();
        }

        InfoHelper.sendInfo(otfc.get(), src, chatUtil, infoConfigAdapter.getNodeOrDefault().getMotdTitle());
        return CommandResult.success();
    }
}

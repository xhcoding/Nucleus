/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Permission is "quickstart.mail.base", because a player should always be able
 * to clear mail if they can read it.
 */
@Permissions(mainOverride = "mail", suggestedLevel = SuggestedLevel.USER)
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
@RegisterCommand(value = "clear", subcommandOf = MailCommand.class)
public class ClearMailCommand extends AbstractCommand<Player> {

    @Inject private MailHandler handler;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (handler.clearUserMail(src)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.mail.clear.success"));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.mail.clear.nomail"));
        }
        return CommandResult.success();
    }
}

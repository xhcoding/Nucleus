/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.mail;

import com.google.inject.Inject;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.NoCooldown;
import io.github.essencepowered.essence.internal.annotations.NoCost;
import io.github.essencepowered.essence.internal.annotations.NoWarmup;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.annotations.RunAsync;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.MailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Permission is "quickstart.mail.base", because a player should always be able
 * to clear mail if they can read it.
 */
@Permissions(alias = "mail", suggestedLevel = SuggestedLevel.USER)
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
@RegisterCommand(value = "clear", subcommandOf = MailCommand.class)
public class ClearMailCommand extends CommandBase<Player> {

    @Inject private MailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        handler.clearUserMail(src);
        src.sendMessage(Util.getTextMessageWithFormat("command.mail.clear"));
        return CommandResult.success();
    }
}

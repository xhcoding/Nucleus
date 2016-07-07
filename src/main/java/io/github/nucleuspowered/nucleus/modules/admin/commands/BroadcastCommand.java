/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.admin.config.AdminConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.admin.config.BroadcastConfig;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.List;

@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@Permissions
@RegisterCommand({ "broadcast", "bcast", "bc" })
public class BroadcastCommand extends CommandBase<CommandSource> {
    private final String message = "message";
    @Inject private AdminConfigAdapter adminConfigAdapter;
    @Inject private ChatUtil chatUtil;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] { GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message))) };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String m = args.<String>getOne(message).get();
        BroadcastConfig bc = adminConfigAdapter.getNodeOrDefault().getBroadcastMessage();
        List<String> messages = Lists.newArrayList();

        String p = bc.getPrefix();
        if (!p.trim().isEmpty()) {
            messages.add(p);
        }

        messages.add(m);

        String s = bc.getSuffix();
        if (!s.trim().isEmpty()) {
            messages.add(s);
        }

        MessageChannel.TO_ALL.send(src, chatUtil.getPlayerMessageFromTemplate(String.join(" ", messages.toArray(new CharSequence[messages.size()])), src, true));
        return CommandResult.success();
    }
}

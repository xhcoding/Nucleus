/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.admin.config.AdminConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

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
        MessageChannel.TO_ALL.send(src, constructMessage(m));
        return CommandResult.success();
    }

    private Text constructMessage(String message) {
        String colour = adminConfigAdapter.getNodeOrDefault().getColorCode().substring(0, 1);
        if (colour.isEmpty()) {
            colour = "a";
        }

        String tag = adminConfigAdapter.getNodeOrDefault().getTag();
        return Text.builder().append(TextSerializers.formattingCode('&').deserialize(tag))
                .append(chatUtil.addUrlsToAmpersandFormattedString(" &" + colour + message)).build();
    }
}

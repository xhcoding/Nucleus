/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.admin;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

@RunAsync
@Modules(PluginModule.ADMIN)
@NoCooldown
@NoCost
@NoWarmup
@Permissions
@RegisterCommand({ "broadcast", "bcast", "bc" })
public class BroadcastCommand extends CommandBase<CommandSource> {
    private final String message = "message";
    @Inject private CommandsConfig commandsConfig;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String m = args.<String>getOne(message).get();
        MessageChannel.TO_ALL.send(src, constructMessage(m));
        return CommandResult.success();
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = super.getDefaults();
        ccn.getNode("broadcast-tag").setComment(Util.getMessageWithFormat("config.broadcast.tag")).setValue(Util.getMessageWithFormat("broadcast.tag"));
        ccn.getNode("broadcast-colour").setComment(Util.getMessageWithFormat("config.broadcast.msg")).setValue("a");
        return ccn;
    }

    private Text constructMessage(String message) {
        CommentedConfigurationNode ccn = commandsConfig.getCommandNode(getAliases()[0]);
        String colour = ccn.getNode("broadcast-colour").getString("a").substring(0, 1);
        String tag = ccn.getNode("broadcast-tag").getString(Util.getMessageWithFormat("broadcast.tag"));
        return Text.builder().append(TextSerializers.formattingCode('&').deserialize(tag))
                .append(TextSerializers.formattingCode('&').deserialize(" &" + colour + message)).build();
    }
}

package uk.co.drnaylor.minecraft.quickstart.commands.message;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;

/**
 * Replies to the last player who sent a message
 *
 * Permission: quickstart.message.base
 */
@Permissions(alias = "message")
@Modules(PluginModule.MESSAGES)
@RunAsync
public class ReplyCommand extends CommandBase {
    private final String message = "message";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))
            ).description(Text.of("Send a message to the player you just sent a message to.")).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "reply", "r" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean b = plugin.getMessageHandler().replyMessage(src, args.<String>getOne(message).get());
        return b ? CommandResult.success() : CommandResult.empty();
    }
}

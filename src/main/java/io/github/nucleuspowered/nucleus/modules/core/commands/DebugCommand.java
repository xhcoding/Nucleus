/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.Scan;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.stream.Collectors;

@Scan
@Permissions(prefix = "nucleus")
@RegisterCommand(value = "debug", subcommandOf = NucleusCommand.class, hasExecutor = false)
public class DebugCommand extends AbstractCommand<CommandSource> {

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        return null;
    }

    @Permissions(prefix = "nucleus.debug")
    @RegisterCommand(value = "getuuids", subcommandOf = DebugCommand.class)
    public static class GetUUIDSCommand extends AbstractCommand<CommandSource> {

        private final String userName = "user";

        @Override public CommandElement[] getArguments() {
            return new CommandElement[] {
                new NicknameArgument(Text.of(userName), plugin.getUserDataManager(), NicknameArgument.UnderlyingType.USER, false)
            };
        }

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            Collection<User> users = args.getAll(userName);
            if (users.isEmpty()) {
                throw ReturnMessageException.fromKey("command.nucleus.debug.uuid.none");
            }

            MessageProvider provider = plugin.getMessageProvider();
            Util.getPaginationBuilder(src)
                .title(provider.getTextMessageWithFormat("command.nucleus.debug.uuid.title", users.iterator().next().getName()))
                .header(provider.getTextMessageWithFormat("command.nucleus.debug.uuid.header"))
                .contents(
                    users.stream()
                        .map(
                            x -> Text.builder(x.getUniqueId().toString()).color(x.isOnline() ? TextColors.GREEN : TextColors.RED)
                                .onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat(
                                    "command.nucleus.debug.uuid.clicktodelete"
                                )))
                        .onClick(TextActions.runCommand("/nucleus resetuser -a " + x.getUniqueId().toString()))
                        .build()
                    ).collect(Collectors.toList())
                ).sendTo(src);
            return CommandResult.success();
        }
    }
}

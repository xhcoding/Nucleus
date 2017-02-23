/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit.command;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import java.util.List;

@NoCooldown
@NoCost
@NoWarmup
@RunAsync
@Permissions(prefix = "kit.command", suggestedLevel = SuggestedLevel.NONE)
@RegisterCommand(value = {"remove", "del", "-"}, subcommandOf = KitCommandCommand.class)
public class KitRemoveCommandCommand extends AbstractCommand<CommandSource> {

    private final String key = "kit";
    private final String index = "index";
    private final String command = "command";

    @Inject private KitHandler handler;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new KitArgument(Text.of(key), false),
            GenericArguments.firstParsing(
                new PositiveIntegerArgument(Text.of(index)),
                new RemainingStringsArgument(Text.of(command)))
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        KitArgument.KitInfo kitInfo = args.<KitArgument.KitInfo>getOne(key).get();
        List<String> commands = kitInfo.kit.getCommands();

        String cmd;
        if (args.hasAny(index)) {
            int idx = args.<Integer>getOne(index).get();
            if (idx == 0) {
                throw ReturnMessageException.fromKey("command.kit.command.remove.onebased");
            }

            if (idx > commands.size()) {
                throw ReturnMessageException.fromKey("command.kit.command.remove.overidx", String.valueOf(commands.size()), kitInfo.name);
            }

            cmd = commands.remove(idx - 1);
        } else {
            cmd = args.<String>getOne(command).get().replace(" {player} ", " {{player}} ");
            if (!commands.remove(cmd)) {
                throw ReturnMessageException.fromKey("command.kit.command.remove.noexist", cmd, kitInfo.name);
            }
        }

        kitInfo.kit.setCommands(commands);
        handler.saveKit(kitInfo.name, kitInfo.kit);
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.remove.success", cmd, kitInfo.name));
        return CommandResult.success();
    }
}

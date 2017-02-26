/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit.command;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

@NoCooldown
@NoCost
@NoWarmup
@RunAsync
@Permissions(prefix = "kit.command", suggestedLevel = SuggestedLevel.NONE)
@RegisterCommand(value = {"add", "+"}, subcommandOf = KitCommandCommand.class)
public class KitAddCommandCommand extends AbstractCommand<CommandSource> {

    private final String key = "kit";
    private final String command = "command";

    @Inject private KitHandler handler;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new KitArgument(Text.of(key), false),
            new RemainingStringsArgument(Text.of(command))
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        KitArgument.KitInfo kitInfo = args.<KitArgument.KitInfo>getOne(key).get();
        String c = args.<String>getOne(command).get().replace(" {player} ", " {{player}} ");
        kitInfo.kit.addCommand(c);
        handler.saveKit(kitInfo.name, kitInfo.kit);

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.add.command", c, kitInfo.name));
        return CommandResult.success();
    }
}

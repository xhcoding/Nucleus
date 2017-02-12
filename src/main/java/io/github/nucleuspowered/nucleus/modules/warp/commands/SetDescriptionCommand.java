/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.WarpArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@RunAsync
@NoCost
@NoCooldown
@NoWarmup
@Permissions(prefix = "warp")
@RegisterCommand(value = {"setdescription"}, subcommandOf = WarpCommand.class)
public class SetDescriptionCommand extends AbstractCommand<CommandSource> {

    private final String warpKey = "warp";
    private final String descriptionKey = "description";
    @Inject private WarpConfigAdapter warpConfigAdapter;
    @Inject private WarpHandler handler;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("r", "-remove", "-delete").buildWith(
                GenericArguments.seq(
                    new WarpArgument(Text.of(warpKey), warpConfigAdapter, false, false),
                    GenericArguments.optional(new RemainingStringsArgument(Text.of(descriptionKey)))
                )
            )
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String warpName = args.<Warp>getOne(warpKey).get().getName();
        if (args.hasAny("r")) {
            // Remove the desc.
            if (handler.setWarpDescription(warpName, null)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.description.removed", warpName));
                return CommandResult.success();
            }

            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.description.noremove", warpName));
        }

        // Add the category.
        Text message = TextSerializers.FORMATTING_CODE.deserialize(args.<String>getOne(descriptionKey).get());
        if (handler.setWarpDescription(warpName, message)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithTextFormat("command.warp.description.added", message, Text.of(warpName)));
            return CommandResult.success();
        }

        throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithTextFormat("command.warp.description.couldnotadd",
                Text.of(warpName)));
    }
}

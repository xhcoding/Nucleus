/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.data.Home;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeOtherArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.events.DeleteHomeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;

@Permissions(prefix = "home", mainOverride = "deleteother")
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"deletehomeother", "delhomeother"})
public class DeleteOtherHomeCommand extends AbstractCommand<CommandSource> {

    private final String homeKey = "home";

    @Inject private CoreConfigAdapter cca;
    @Inject private UserDataManager udm;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new HomeOtherArgument(Text.of(homeKey), plugin, cca))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Home wl = args.<Home>getOne(homeKey).get();

        DeleteHomeEvent event = new DeleteHomeEvent(Cause.of(NamedCause.owner(src)), wl);
        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(event.getCancelMessage().orElseGet(() ->
                plugin.getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")
            ));
        }

        if (udm.get(wl.getUniqueId()).get().deleteHome(wl.getName())) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.delete.other.success", wl.getUser().getName(), wl.getName()));
            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.delete.other.fail", wl.getUser().getName(), wl.getName()));
        return CommandResult.empty();
    }
}

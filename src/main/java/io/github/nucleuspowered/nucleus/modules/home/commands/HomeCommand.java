/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.data.Home;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.HomeModule;
import io.github.nucleuspowered.nucleus.modules.home.config.HomeConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.events.UseHomeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("home")
public class HomeCommand extends AbstractCommand<Player> {

    private final String home = "home";

    @Inject private CoreConfigAdapter cca;
    @Inject private HomeConfigAdapter homeConfigAdapter;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.optional(new HomeArgument(Text.of(home), plugin, cca)))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        Optional<Home> owl = args.getOne(home);
        if (!owl.isPresent()) {
            owl = plugin.getUserDataManager().get(src).get().getHome(HomeModule.DEFAULT_HOME_NAME);

            if (!owl.isPresent()) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("args.home.nohome", "home"));
                return CommandResult.empty();
            }
        }

        Home wl = owl.get();

        if (!wl.getLocation().isPresent()) {
            // Fail
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.home.invalid", wl.getName()));
        }

        UseHomeEvent event = new UseHomeEvent(Cause.of(NamedCause.owner(src)), src, wl);
        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(event.getCancelMessage().orElseGet(() ->
                plugin.getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")
            ));
        }

        // Warp to it safely.
        if (plugin.getTeleportHandler().teleportPlayer(src, wl.getLocation().get(), wl.getRotation(), homeConfigAdapter.getNodeOrDefault().isSafeTeleport())) {
            if (!wl.getName().equalsIgnoreCase(HomeModule.DEFAULT_HOME_NAME)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.success", wl.getName()));
            } else {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.successdefault"));
            }

            return CommandResult.success();
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.fail", wl.getName()));
            return CommandResult.empty();
        }
    }
}

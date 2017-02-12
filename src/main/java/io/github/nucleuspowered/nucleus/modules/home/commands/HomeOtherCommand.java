/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Home;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeOtherArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
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

import java.util.HashMap;
import java.util.Map;

@Permissions(prefix = "home", mainOverride = "other", suggestedLevel = SuggestedLevel.MOD)
@RegisterCommand("homeother")
public class HomeOtherCommand extends AbstractCommand<Player> {

    private final String home = "home";
    public static final String OTHER_EXEMPT_PERM_SUFFIX = "exempt.target";

    @Inject private CoreConfigAdapter cca;
    @Inject private HomeConfigAdapter homeConfigAdapter;

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("exempt.target", PermissionInformation.getWithTranslation("permission.home.other.exempt.target", SuggestedLevel.ADMIN));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new HomeOtherArgument(Text.of(home), plugin, cca))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        Home wl = args.<Home>getOne(home).get();

        if (!wl.getLocation().isPresent()) {
            // Fail
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.homeother.invalid", wl.getUser().getName(), wl.getName()));
            return CommandResult.empty();
        }

        UseHomeEvent event = new UseHomeEvent(Cause.of(NamedCause.owner(src)), src, wl);
        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(event.getCancelMessage().orElseGet(() ->
                plugin.getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")
            ));
        }

        // Warp to it safely.
        if (plugin.getTeleportHandler().teleportPlayer(src, wl.getLocation().get(), wl.getRotation(), homeConfigAdapter.getNodeOrDefault().isSafeTeleport())) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.homeother.success", wl.getUser().getName(), wl.getName()));
            return CommandResult.success();
        } else {
            throw ReturnMessageException.fromKey("command.homeother.fail", wl.getUser().getName(), wl.getName());
        }
    }
}

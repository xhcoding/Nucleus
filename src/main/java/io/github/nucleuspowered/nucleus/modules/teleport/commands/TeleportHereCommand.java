/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * NOTE: TeleportHere is considered an admin command, as there is a potential
 * for abuse for non-admin players trying to pull players. No cost or warmups
 * will be applied. /tpahere should be used instead in these circumstances.
 */
@Permissions(prefix = "teleport", suggestedLevel = SuggestedLevel.ADMIN, supportsSelectors = true)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"tphere", "tph"})
@EssentialsEquivalent(value = {"tphere", "s", "tpohere"}, isExact = false,
        notes = "If you have permission, this will override '/tptoggle' automatically.")
public class TeleportHereCommand extends AbstractCommand<Player> {

    private final String playerKey = "subject";
    private final String quietKey = "quiet";

    @Inject private TeleportHandler handler;
    @Inject private TeleportConfigAdapter tca;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(
                SelectorWrapperArgument.nicknameSelector(Text.of(playerKey), NicknameArgument.UnderlyingType.PLAYER)
            )
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        boolean beQuiet = args.<Boolean>getOne(quietKey).orElse(tca.getNodeOrDefault().isDefaultQuiet());
        Player target = args.<Player>getOne(playerKey).get();
        handler.getBuilder().setFrom(target).setTo(src).setSilentSource(beQuiet).startTeleport();
        return CommandResult.success();
    }
}

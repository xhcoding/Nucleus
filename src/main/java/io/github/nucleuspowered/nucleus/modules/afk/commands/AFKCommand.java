/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

@RegisterCommand("afk")
@Permissions(suggestedLevel = SuggestedLevel.USER)
@NoCooldown
@NoWarmup
@NoCost
@RunAsync
public class AFKCommand extends CommandBase<Player> {

    @Inject private AFKHandler afkHandler;
    @Inject private AFKConfigAdapter aca;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (permissions.testSuffix(src, "exempt.toggle")) {
            src.sendMessage(Util.getTextMessageWithFormat("command.afk.exempt"));
            return CommandResult.empty();
        }

        boolean isAFK = afkHandler.getAFKData(src).isAFK();

        if (isAFK) {
            afkHandler.updateUserActivity(src.getUniqueId());
        } else {
            afkHandler.setAFK(src.getUniqueId(), true);
        }

        if (!aca.getNodeOrDefault().isAfkOnVanish() && src.get(Keys.INVISIBLE).orElse(false)) {
            // This tells the user they have gone AFK, but no one else.
            src.sendMessage(Util.getTextMessageWithFormat(isAFK ? "command.afk.from.vanish" : "command.afk.to.vanish"));
        }

        return CommandResult.success();
    }
}

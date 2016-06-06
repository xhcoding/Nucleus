/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@Permissions
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"vanish", "v"})
public class VanishCommand extends CommandBase<Player> {

    private final String b = "toggle";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(b))))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // If we don't specify whether to vanish, toggle
        boolean toVanish = args.<Boolean>getOne(b).orElse(!src.get(Keys.INVISIBLE).orElse(false));

        DataTransactionResult dtr = src.offer(Keys.INVISIBLE, toVanish);
        src.offer(Keys.INVISIBILITY_IGNORES_COLLISION, toVanish);
        src.offer(Keys.INVISIBILITY_PREVENTS_TARGETING, toVanish);
        src.offer(Keys.IS_SILENT, toVanish);
        if (dtr.isSuccessful()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.vanish.success",
                    toVanish ? Util.getMessageWithFormat("command.vanish.vanished") : Util.getMessageWithFormat("command.vanish.visible")));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.vanish.fail"));
        return CommandResult.empty();
    }
}

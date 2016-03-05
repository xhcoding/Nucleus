/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.vanish;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@Modules(PluginModule.VANISH)
@Permissions
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"vanish", "v"})
public class VanishCommand extends CommandBase<Player> {

    private final String b = "toggle";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(b))))).executor(this)
                .build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // If we don't specify whether to vanish, toggle
        boolean toVanish = args.<Boolean>getOne(b).orElse(!src.get(Keys.INVISIBLE).orElse(false));

        DataTransactionResult dtr = src.offer(Keys.INVISIBLE, toVanish);
        src.offer(Keys.INVISIBILITY_IGNORES_COLLISION, toVanish);
        src.offer(Keys.INVISIBILITY_PREVENTS_TARGETING, toVanish);
        if (dtr.isSuccessful()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.vanish.success",
                    toVanish ? Util.getMessageWithFormat("command.vanish.vanished") : Util.getMessageWithFormat("command.vanish.visible")));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.vanish.fail"));
        return CommandResult.empty();
    }
}

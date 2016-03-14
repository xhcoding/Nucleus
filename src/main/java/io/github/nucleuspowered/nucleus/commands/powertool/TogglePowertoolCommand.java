/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.powertool;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * Toggles whether powetools will activate for this user.
 *
 * Permission: nucleus.powertool.base (uses the base permission)
 */
@Permissions(alias = "powertool")
@RunAsync
@NoCooldown
@NoWarmup
@NoCost
@RegisterCommand(value = {"toggle"}, subcommandOf = PowertoolCommand.class)
public class TogglePowertoolCommand extends CommandBase<Player> {

    private final String toggleKey = "toggle";
    @Inject private UserConfigLoader loader;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(toggleKey))))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        InternalNucleusUser user = loader.getUser(src);

        // If specified - get the key. Else, the inverse of what we have now.
        boolean toggle = args.<Boolean>getOne(toggleKey).orElse(!user.isPowertoolToggled());
        user.setPowertoolToggle(toggle);

        src.sendMessage(Util.getTextMessageWithFormat("command.powertool.toggle", Util.getMessageWithFormat(toggle ? "enabled" : "disabled")));
        return CommandResult.success();
    }
}

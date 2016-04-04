/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@RegisterCommand({"delnick", "delnickname", "deletenick"})
@Permissions(alias = "nick")
public class DelNickCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader loader;

    private final String playerKey = "player";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.user(Text.of(playerKey))),
                        permissions.getPermissionWithSuffix("others")))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<User> opl = this.getUser(User.class, src, playerKey, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        User pl = opl.get();
        InternalNucleusUser internalQuickStartUser = loader.getUser(pl);
        internalQuickStartUser.removeNickname();

        if (!src.equals(pl)) {
            src.sendMessage(Util.getTextMessageWithFormat("command.delnick.success.other", pl.getName()));
        }

        if (pl.isOnline()) {
            pl.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("command.delnick.success"));
        }

        return CommandResult.success();
    }
}

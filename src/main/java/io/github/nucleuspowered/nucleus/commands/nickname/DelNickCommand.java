/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.nickname;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@RegisterCommand({"delnick", "delnickname", "deletenick"})
@Permissions(alias = "nick")
@Modules(PluginModule.NICKNAME)
public class DelNickCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader loader;

    private final String playerKey = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .arguments(
                        GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.user(Text.of(playerKey))),
                                permissions.getPermissionWithSuffix("others"))))
                .executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<User> opl = this.getUser(User.class, src, playerKey, args);
        if (opl.isPresent()) {
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

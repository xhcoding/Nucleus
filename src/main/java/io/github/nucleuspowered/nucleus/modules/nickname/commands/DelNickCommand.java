/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.commands;

import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.nickname.datamodules.NicknameUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@RegisterCommand({"delnick", "delnickname", "deletenick"})
@Permissions(mainOverride = "nick")
public class DelNickCommand extends AbstractCommand<CommandSource> {

    @Inject private UserDataManager loader;

    private final String playerKey = "subject";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.user(Text.of(playerKey))),
                        permissions.getPermissionWithSuffix("others")))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = this.getUserFromArgs(User.class, src, playerKey, args);
        ModularUserService userService = loader.get(pl).get();
        userService.get(NicknameUserDataModule.class).removeNickname();

        if (!src.equals(pl)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.delnick.success.other", pl.getName()));
        }

        if (pl.isOnline()) {
            pl.getPlayer().get().sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.delnick.success.base"));
        }

        return CommandResult.success();
    }
}

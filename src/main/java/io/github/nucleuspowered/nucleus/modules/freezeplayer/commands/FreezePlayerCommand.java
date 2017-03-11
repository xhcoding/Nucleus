/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.freezeplayer.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.datamodules.FreezePlayerUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

@Permissions(supportsOthers = true)
@RegisterCommand({"freezeplayer", "freeze"})
public class FreezePlayerCommand extends AbstractCommand<CommandSource> {

    @Inject private UserDataManager userConfigLoader;

    private final String player = "subject";
    private final String truefalsekey = "true|false";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                        GenericArguments.onlyOne(SelectorWrapperArgument.nicknameSelector(
                                Text.of(player),
                                NicknameArgument.UnderlyingType.USER
                        )), permissions.getPermissionWithSuffix("others"))),
                GenericArguments.optional(GenericArguments.bool(Text.of(truefalsekey)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = this.getUserFromArgs(User.class, src, player, args);
        FreezePlayerUserDataModule nu = userConfigLoader.getUnchecked(pl).get(FreezePlayerUserDataModule.class);
        nu.setFrozen(args.<Boolean>getOne(truefalsekey).orElseGet(() -> !nu.isFrozen()));
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat(
            nu.isFrozen() ? "command.freezeplayer.success.frozen" : "command.freezeplayer.success.unfrozen", plugin.getNameUtil().getSerialisedName(pl)));
        return CommandResult.success();
    }
}

/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.nickname;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.RequireOneOfPermission;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.util.Optional;

@RootCommand
@Permissions(alias = "nick")
@Modules(PluginModule.NICKNAME)
public class DelNickCommand extends CommandBase {
    @Inject
    private UserConfigLoader loader;

    private final String playerKey = "player";
    private final String nickName = "nickname";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.optionalWeak(new RequireOneOfPermission(GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey))), permissions.getPermissionWithSuffix("other")))
        ).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "delnick", "delnickname", "deletenick" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<User> opl = args.<User>getOne(playerKey);
        User pl;
        if (opl.isPresent()) {
            pl = opl.get();
        } else if (src instanceof User) {
            pl = (User)src;
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.playeronly")));
            return CommandResult.empty();
        }

        InternalQuickStartUser internalQuickStartUser = loader.getUser(pl);
        internalQuickStartUser.removeNickname();

        if (!src.equals(pl)) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.delnick.success.other", pl.getName())));
        }

        if (pl.isOnline()) {
            pl.getPlayer().get().sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.delnick.success")));
        }

        return CommandResult.success();
    }
}

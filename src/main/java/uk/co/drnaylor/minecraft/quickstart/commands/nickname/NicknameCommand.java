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
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.util.Optional;
import java.util.regex.Pattern;

@RootCommand
@Permissions
@Modules(PluginModule.NICKNAME)
public class NicknameCommand extends CommandBase {

    @Inject private UserConfigLoader loader;

    private final String playerKey = "player";
    private final String nickName = "nickname";

    private final Pattern colourPattern = Pattern.compile("&[0-9a-f]", Pattern.CASE_INSENSITIVE);
    private final Pattern stylePattern = Pattern.compile("&[omn]", Pattern.CASE_INSENSITIVE);
    private final Pattern magicPattern = Pattern.compile("&[0-9a-f]", Pattern.CASE_INSENSITIVE);

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.optionalWeak(new RequireOneOfPermission(GenericArguments.onlyOne(new UserParser(Text.of(playerKey))), permissions.getPermissionWithSuffix("other"))),
                GenericArguments.onlyOne(GenericArguments.string(Text.of(nickName)))
        ).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "nick", "nickname" };
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

        String name = args.<String>getOne(nickName).get();

        // Giving player must have the colour permissions and whatnot. Also, colour and color are the two spellings we support. (RULE BRITANNIA!)
        if (colourPattern.matcher(name).find() && permissions.getPermissionsWithSuffixes(PermissionUtil.PermissionLevel.ADMIN, "colour", "color").stream().noneMatch(src::hasPermission)) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.nick.colour.noperms")));
            return CommandResult.empty();
        }

        // Giving player must have the colour permissions and whatnot.
        if (magicPattern.matcher(name).find() && permissions.getPermissionWithSuffix("magic").stream().noneMatch(src::hasPermission)) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.nick.magic.noperms")));
            return CommandResult.empty();
        }

        // Giving player must have the colour permissions and whatnot.
        if (stylePattern.matcher(name).find() && permissions.getPermissionWithSuffix("style").stream().noneMatch(src::hasPermission)) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.nick.style.noperms")));
            return CommandResult.empty();
        }

        InternalQuickStartUser internalQuickStartUser = loader.getUser(pl);
        internalQuickStartUser.setNickname(name);
        Text set = internalQuickStartUser.getNicknameAsText().get();

        if (!src.equals(pl)) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.nick.success.other", pl.getName()) + " - ",
                    TextColors.RESET, set));
        }

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.nick.success") + " - ",
                TextColors.RESET, set));
        return CommandResult.success();
    }
}

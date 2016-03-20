/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RegisterCommand({"nick", "nickname"})
@Permissions
public class NicknameCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader loader;
    @Inject private NicknameConfigAdapter nicknameConfigAdapter;

    private final String playerKey = "player";
    private final String nickName = "nickname";

    private final Pattern colourPattern = Pattern.compile("&[0-9a-f]", Pattern.CASE_INSENSITIVE);
    private final Pattern stylePattern = Pattern.compile("&[omnl]", Pattern.CASE_INSENSITIVE);
    private final Pattern magicPattern = Pattern.compile("&k", Pattern.CASE_INSENSITIVE);

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.nick.others"), SuggestedLevel.ADMIN));
        m.put("colour", new PermissionInformation(Util.getMessageWithFormat("permission.nick.colour"), SuggestedLevel.ADMIN));
        m.put("color", new PermissionInformation(Util.getMessageWithFormat("permission.nick.colour"), SuggestedLevel.ADMIN));
        m.put("style", new PermissionInformation(Util.getMessageWithFormat("permission.nick.style"), SuggestedLevel.ADMIN));
        m.put("magic", new PermissionInformation(Util.getMessageWithFormat("permission.nick.magic"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .arguments(
                        GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                                GenericArguments.onlyOne(GenericArguments.user(Text.of(playerKey))), permissions.getPermissionWithSuffix("others"))),
                GenericArguments.onlyOne(GenericArguments.string(Text.of(nickName)))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<User> opl = this.getUser(User.class, src, playerKey, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        User pl = opl.get();
        String name = args.<String>getOne(nickName).get();

        // Giving player must have the colour permissions and whatnot. Also,
        // colour and color are the two spellings we support. (RULE BRITANNIA!)
        if (colourPattern.matcher(name).find() && !(permissions.testSuffix(src, "colour") || permissions.testSuffix(src, "color"))) {
            src.sendMessage(Util.getTextMessageWithFormat("command.nick.colour.noperms"));
            return CommandResult.empty();
        }

        // Giving player must have the colour permissions and whatnot.
        if (magicPattern.matcher(name).find() && !permissions.testSuffix(src, "magic")) {
            src.sendMessage(Util.getTextMessageWithFormat("command.nick.magic.noperms"));
            return CommandResult.empty();
        }

        // Giving player must have the colour permissions and whatnot.
        if (stylePattern.matcher(name).find() && !permissions.testSuffix(src, "style")) {
            src.sendMessage(Util.getTextMessageWithFormat("command.nick.style.noperms"));
            return CommandResult.empty();
        }

        // Do a regex remove to check minimum length requirements.
        if (name.replaceAll("&[0-9a-fomlnk]", "").length() < Math.max(nicknameConfigAdapter.getNodeOrDefault().getMinNicknameLength(), 1)) {
            src.sendMessage(Util.getTextMessageWithFormat("command.nick.tooshort"));
            return CommandResult.empty();
        }

        InternalNucleusUser internalQuickStartUser = loader.getUser(pl);
        internalQuickStartUser.setNickname(name);
        Text set = internalQuickStartUser.getNicknameAsText().get();

        if (!src.equals(pl)) {
            src.sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.nick.success.other", pl.getName()))
                    .append(Text.of(" - ", TextColors.RESET, set)).build());
        }

        if (pl.isOnline()) {
            pl.getPlayer().get().sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.nick.success"))
                    .append(Text.of(" - ", TextColors.RESET, set)).build());
        }

        return CommandResult.success();
    }
}

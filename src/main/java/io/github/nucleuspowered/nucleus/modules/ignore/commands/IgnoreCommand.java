/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ignore.commands;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Map;

@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand("ignore")
@Permissions(suggestedLevel = SuggestedLevel.USER)
public class IgnoreCommand extends CommandBase<Player> {

    @Inject private UserConfigLoader loader;

    private final String userKey = "user";
    private final String toggleKey = "toggle";

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = Maps.newHashMap();
        m.put("exempt.chat", new PermissionInformation(Util.getMessageWithFormat("permission.ignore.chat"), SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(GenericArguments.user(Text.of(userKey))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(toggleKey))))
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the target
        User target = args.<User>getOne(userKey).get();

        if (target.equals(src)) {
            src.sendMessage(Util.getTextMessageWithFormat("command.ignore.self"));
            return CommandResult.empty();
        }

        InternalNucleusUser inu = loader.getUser(src);

        if (permissions.testSuffix(target, "exempt.chat")) {
            // Make sure they are removed.
            inu.removeFromIgnoreList(target.getUniqueId());
            src.sendMessage(Util.getTextMessageWithFormat("command.ignore.exempt"));
            return CommandResult.empty();
        }

        // Ok, we can ignore or unignore them.
        boolean ignore = args.<Boolean>getOne(toggleKey).orElse(!inu.getIgnoreList().contains(target.getUniqueId()));

        if (ignore) {
            inu.addToIgnoreList(target.getUniqueId());
            src.sendMessage(Util.getTextMessageWithFormat("command.ignore.added", target.getName()));
        } else {
            inu.removeFromIgnoreList(target.getUniqueId());
            src.sendMessage(Util.getTextMessageWithFormat("command.ignore.remove", target.getName()));
        }

        return CommandResult.success();
    }
}

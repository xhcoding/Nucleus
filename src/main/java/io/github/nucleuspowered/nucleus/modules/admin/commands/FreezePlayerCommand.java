/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions
@RegisterCommand({"freezeplayer", "freeze"})
public class FreezePlayerCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader userConfigLoader;

    private final String player = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.others", this.getAliases()[0]), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.requiringPermission(
                        GenericArguments.onlyOne(GenericArguments.player(Text.of(player))), permissions.getPermissionWithSuffix("others")))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = this.getUser(Player.class, src, player, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        NucleusUser nu;

        try {
            nu = userConfigLoader.getUser(opl.get());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(Util.getTextMessageWithFormat("command.file.load"), e);
        }

        if (nu.isFrozen()) {
            nu.setFrozen(false);
        } else {
            nu.setFrozen(true);
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.freezeplayer.success", opl.get().getName(), nu.isFrozen() ? "frozen" : "un-frozen"));
        return CommandResult.success();
    }
}

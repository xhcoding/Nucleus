/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.misc;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.interfaces.InternalQuickStartUser;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions
@Modules(PluginModule.MISC)
@RegisterCommand("fly")
public class FlyCommand extends CommandBase<CommandSource> {
    private static final String player = "player";
    private static final String toggle = "toggle";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.others", this.getAliases()[0]), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.requiringPermission(GenericArguments.player(Text.of(player)), permissions.getPermissionWithSuffix("others")))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(toggle))))
        ).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = this.getUser(Player.class, src, player, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        Player pl = opl.get();

        InternalQuickStartUser uc = plugin.getUserLoader().getUser(pl);
        boolean fly = args.<Boolean>getOne(toggle).orElse(!uc.isFlying());

        if (!uc.setFlying(fly)) {
            src.sendMessages(Text.of(TextColors.RED, Util.getMessageWithFormat("command.fly.error")));
            return CommandResult.empty();
        }

        if (pl != src) {
            src.sendMessages(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat(fly ? "command.fly.player.on" : "command.fly.player.off"), pl.getName())));
        }

        pl.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat(fly ? "command.fly.on" : "command.fly.off")));
        return CommandResult.success();
    }
}

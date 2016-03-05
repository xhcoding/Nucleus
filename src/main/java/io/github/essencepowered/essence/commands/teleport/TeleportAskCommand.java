/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.NoWarmup;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.annotations.RunAsync;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Sends a request to a player to teleport to them, using click handlers.
 */
@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.TELEPORT)
@NoWarmup(generateConfigEntry = true)
@RegisterCommand({"tpa", "teleportask"})
@RunAsync
public class TeleportAskCommand extends CommandBase<Player> {

    @Inject private TeleportHandler tpHandler;

    private final String playerKey = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("force", new PermissionInformation(Util.getMessageWithFormat("permission.teleport.force"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .arguments(GenericArguments.requiringPermission(GenericArguments.flags().flag("f").buildWith(GenericArguments.none()),
                        permissions.getPermissionWithSuffix("force")), GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey))))
                .executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player target = args.<Player>getOne(playerKey).get();
        if (src.equals(target)) {
            src.sendMessage(Util.getTextMessageWithFormat("command.teleport.self"));
            return CommandResult.empty();
        }

        TeleportHandler.TeleportBuilder tb = tpHandler.getBuilder().setFrom(src).setTo(target).setSafe(!args.<Boolean>getOne("f").orElse(false));
        int warmup = getWarmup(src);
        if (warmup > 0) {
            tb.setWarmupTime(warmup);
        }

        double cost = getCost(src, args);
        if (cost > 0.) {
            tb.setCharge(src).setCost(cost);
        }

        tpHandler.addAskQuestion(target.getUniqueId(), new TeleportHandler.TeleportPrep(Instant.now().plus(30, ChronoUnit.SECONDS), src, cost, tb));
        target.sendMessage(Util.getTextMessageWithFormat("command.tpa.question", src.getName()));
        target.sendMessage(tpHandler.getAcceptDenyMessage());

        src.sendMessage(Util.getTextMessageWithFormat("command.tpask.sent", target.getName()));
        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Sends a request to a subject to teleport to them, using click handlers.
 */
@SuppressWarnings("ALL")
@Permissions(prefix = "teleport", suggestedLevel = SuggestedLevel.USER, supportsSelectors = true)
@NoWarmup(generateConfigEntry = true, generatePermissionDocs = true)
@RegisterCommand({"tpa", "teleportask"})
@RunAsync
@NotifyIfAFK(TeleportAskCommand.playerKey)
public class TeleportAskCommand extends StandardAbstractCommand<Player> {

    @Inject private TeleportHandler tpHandler;

    static final String playerKey = "subject";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("force", PermissionInformation.getWithTranslation("permission.teleport.force", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(
                    SelectorWrapperArgument.nicknameSelector(Text.of(playerKey), NicknameArgument.UnderlyingType.PLAYER)
                ),
                GenericArguments.flags().permissionFlag(permissions.getPermissionWithSuffix("force"), "f").buildWith(GenericArguments.none())
        };
    }

    @Override protected ContinueMode preProcessChecks(SubjectPermissionCache<Player> source, CommandContext args) {
        return TeleportHandler.canTeleportTo(source, args.<Player>getOne(playerKey).get()) ? ContinueMode.CONTINUE : ContinueMode.STOP;
    }

    @Override
    public CommandResult executeCommand(SubjectPermissionCache<Player> cache, CommandContext args) throws Exception {
        Player src = cache.getSubject();
        Player target = args.<Player>getOne(playerKey).get();
        if (src.equals(target)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.teleport.self"));
            return CommandResult.empty();
        }

        TeleportHandler.TeleportBuilder tb = tpHandler.getBuilder().setFrom(src).setTo(target).setSafe(!args.<Boolean>getOne("f").orElse(false));
        int warmup = getWarmup(cache);
        if (warmup > 0) {
            tb.setWarmupTime(warmup);
        }

        double cost = getCost(cache, args);
        if (cost > 0.) {
            tb.setCharge(src).setCost(cost);
        }

        tpHandler.addAskQuestion(target.getUniqueId(), new TeleportHandler.TeleportPrep(Instant.now().plus(30, ChronoUnit.SECONDS), src, cost, tb));
        target.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tpa.question", src.getName()));
        target.sendMessage(tpHandler.getAcceptDenyMessage());

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tpask.sent", target.getName()));
        return CommandResult.success();
    }
}

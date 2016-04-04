/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban;

import java.util.Optional;

@RegisterCommand("checkban")
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
public class CheckBanCommand extends CommandBase<CommandSource> {

    private final String key = "player";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(GenericArguments.user(Text.of(key)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User u = args.<User>getOne(key).get();

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        Optional<Ban.Profile> obp = service.getBanFor(u.getProfile());
        if (!obp.isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.checkban.notset", u.getName()));
            return CommandResult.success();
        }

        Ban.Profile bp = obp.get();
        String f = "";
        String t = "";
        if (bp.getExpirationDate().isPresent()) {
            f = Util.getMessageWithFormat("standard.for");
            t = Util.getTimeToNow(bp.getExpirationDate().get());
        }

        String reason;
        if (bp.getBanSource().isPresent()) {
            reason = bp.getBanSource().get().toPlain();
        } else {
            reason = Util.getMessageWithFormat("standard.unknown");
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.checkban.banned", u.getName(), reason, f, t));
        src.sendMessage(Util.getTextMessageWithFormat("standard.reason", bp.getReason().orElse(Util.getTextMessageWithFormat("ban.defaultreason")).toString()));
        return CommandResult.success();
    }
}

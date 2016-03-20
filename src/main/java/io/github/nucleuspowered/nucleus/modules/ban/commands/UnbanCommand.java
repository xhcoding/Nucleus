/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.ban.Ban;

import java.util.Optional;

@RegisterCommand({"unban", "pardon"})
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoWarmup
@NoCooldown
@NoCost
public class UnbanCommand extends CommandBase<CommandSource> {

    private final String key = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of(key)))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User u = args.<User>getOne(key).get();

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        Optional<Ban.Profile> obp = service.getBanFor(u.getProfile());
        if (!obp.isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.checkban.notset", u.getName()));
            return CommandResult.empty();
        }

        service.removeBan(obp.get());

        MutableMessageChannel notify = MessageChannel.permission(BanCommand.notifyPermission).asMutable();
        notify.addMember(src);
        notify.send(Util.getTextMessageWithFormat("command.unban.success", obp.get().getProfile().getName().orElse("standard.unknown"), src.getName()));
        return CommandResult.success();
    }
}
